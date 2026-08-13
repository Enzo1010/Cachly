package br.com.cachly.backend.simulador;

import br.com.cachly.backend.comum.erro.RegraNegocioException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Serviço responsável por executar a simulação didática da memória cache.
 * 
 * Design Arquitetural: Esta classe foi desenhada para ser 100% stateless. 
 * Ela não mantém estado no banco de dados. O algoritmo recebe as configurações e a sequência de 
 * endereços na requisição e processa toda a simulação de forma determinística na memória (heap), 
 * retornando o histórico completo passo a passo. Isso garante alta performance e escabilidade.
 */
@Service
public class SimuladorCacheService {

    /**
     * Executa a simulação completa para a sequência de endereços informada.
     *
     * @param request DTO contendo a topologia da cache e a lista de endereços decimais a serem acessados.
     * @return SimulacaoResponse contendo as estatísticas finais (hits/misses) e o histórico de cada passo.
     */
    public SimulacaoResponse executarSimulacao(SimulacaoRequest request) {
        validarRequest(request);

        int tamanhoCache = request.tamanhoCacheBytes();
        int tamanhoBloco = request.tamanhoBlocoBytes();
        int totalLinhas = tamanhoCache / tamanhoBloco;

        TipoMapeamento mapeamento = request.mapeamento();
        PoliticaSubstituicao substituicao = request.substituicao();

        // Mapeamento e Topologia:
        // Mapeamento Direto: N conjuntos de 1 via.
        // Totalmente Associativo: 1 conjunto de N vias.
        // Associativo por Conjuntos: N conjuntos de M vias.
        int totalConjuntos;
        int numeroVias;

        if (mapeamento == TipoMapeamento.DIRETO) {
            totalConjuntos = totalLinhas;
            numeroVias = 1;
        } else if (mapeamento == TipoMapeamento.TOTALMENTE_ASSOCIATIVO) {
            totalConjuntos = 1;
            numeroVias = totalLinhas;
        } else {
            numeroVias = request.numeroVias();
            totalConjuntos = totalLinhas / numeroVias;
        }

        // Cálculo da divisão de bits do endereço
        // Utilizamos logaritmo na base 2, visto que os tamanhos obrigatoriamente são potências de 2.
        int bitsOffset = log2(tamanhoBloco);
        int bitsIndice = mapeamento == TipoMapeamento.TOTALMENTE_ASSOCIATIVO ? 0 : log2(totalConjuntos);
        
        // Assumindo uma arquitetura genérica de 32 bits para a largura do barramento de endereços.
        int bitsTag = 32 - bitsIndice - bitsOffset;

        List<LinhaCacheInterna> linhas = inicializarLinhas(totalLinhas, totalConjuntos, numeroVias);

        List<PassoSimulacaoResponse> passos = new ArrayList<>();
        int totalHits = 0;
        int totalMisses = 0;
        
        // Relógio lógico para gerenciar as políticas temporais (LRU e FIFO).
        // Ele incrementa a cada evento de acesso ou substituição de bloco na cache.
        long tempoGlobal = 0;

        for (int i = 0; i < request.enderecos().size(); i++) {
            int passoNumero = i + 1;
            int endereco = request.enderecos().get(i);

            /*
             * Extração dos bits usando operadores bit a bit (Bitwise).
             * 
             * 1. Offset: Aplicamos uma máscara com os últimos 'bitsOffset' bits em 1.
             * 2. Indice: Deslocamos o endereço à direita ignorando o offset, e aplicamos a máscara.
             * 3. Tag: Deslocamos o endereço ignorando offset e índice. 
             * NOTA: Utilizamos '>>>' (Unsigned Right Shift) para prevenir preenchimento indevido 
             * com bits de sinal (caso o inteiro extrapole o bit mais significativo no Java).
             */
            int offsetMask = (1 << bitsOffset) - 1;
            int offset = endereco & offsetMask;

            Integer indice = bitsIndice == 0 ? null : (endereco >>> bitsOffset) & ((1 << bitsIndice) - 1);
            int tag = endereco >>> (bitsOffset + bitsIndice);

            // Reduz o escopo de busca apenas para as linhas pertencentes ao conjunto calculado (ou todas se for totalmente associativo)
            List<LinhaCacheInterna> candidatoLinhas = buscarLinhasDoConjunto(linhas, indice, mapeamento, totalLinhas, numeroVias);

            // Verifica se o bloco correspondente à tag já está carregado na memória cache
            LinhaCacheInterna linhaHit = candidatoLinhas.stream()
                    .filter(l -> l.valida && l.tag == tag)
                    .findFirst()
                    .orElse(null);

            boolean isHit = linhaHit != null;
            Integer blocoSubstituido = null;

            if (isHit) {
                totalHits++;
                // Atualiza o marcador de uso para a política LRU (Least Recently Used)
                linhaHit.ultimaUtilizacao = tempoGlobal++;
            } else {
                totalMisses++;
                
                // Em caso de falha (Miss), procura a primeira linha do conjunto que não possui dados válidos (compulsory miss).
                LinhaCacheInterna linhaVazia = candidatoLinhas.stream()
                        .filter(l -> !l.valida)
                        .findFirst()
                        .orElse(null);

                LinhaCacheInterna linhaAlvo;
                if (linhaVazia != null) {
                    linhaAlvo = linhaVazia; // Cold miss: há espaço livre no conjunto
                } else {
                    // Conflict/Capacity miss: conjunto cheio, requer aplicação da política de substituição (Eviction)
                    if (substituicao == PoliticaSubstituicao.LRU) {
                        // LRU: Remove a linha que não é acessada há mais tempo
                        linhaAlvo = candidatoLinhas.stream()
                                .min(Comparator.comparingLong(l -> l.ultimaUtilizacao))
                                .orElseThrow();
                    } else { 
                        // FIFO: Remove a linha mais antiga inserida na cache, independentemente de quando foi acessada por último
                        linhaAlvo = candidatoLinhas.stream()
                                .min(Comparator.comparingLong(l -> l.ordemChegada))
                                .orElseThrow();
                    }
                }

                // Efetua a carga do novo bloco na linha alvo
                linhaAlvo.valida = true;
                linhaAlvo.tag = tag;
                linhaAlvo.ordemChegada = tempoGlobal;
                linhaAlvo.ultimaUtilizacao = tempoGlobal++;
                blocoSubstituido = linhaAlvo.indiceLinha;
            }

            // Realiza um "Snapshot" do estado atual da cache para envio ao Frontend exibir na tabela animada
            List<EstadoLinhaCacheResponse> estadoCache = linhas.stream()
                    .map(l -> new EstadoLinhaCacheResponse(
                            l.indiceLinha,
                            l.conjuntoIndex,
                            l.valida,
                            l.valida ? l.tag : null
                    ))
                    .toList();

            passos.add(new PassoSimulacaoResponse(
                    passoNumero,
                    endereco,
                    tag,
                    indice,
                    offset,
                    isHit,
                    blocoSubstituido,
                    estadoCache
            ));
        }

        int totalAcessos = request.enderecos().size();
        
        // Evita divisão por zero retornando 0.0 caso não haja acessos
        double taxaHit = totalAcessos > 0 ? (double) totalHits / totalAcessos * 100.0 : 0.0;
        double taxaMiss = totalAcessos > 0 ? (double) totalMisses / totalAcessos * 100.0 : 0.0;

        return new SimulacaoResponse(
                bitsOffset,
                bitsIndice,
                bitsTag,
                totalLinhas,
                totalConjuntos,
                totalAcessos,
                totalHits,
                totalMisses,
                arredondar(taxaHit),
                arredondar(taxaMiss),
                passos
        );
    }

    /**
     * Valida as restrições matemáticas e as regras de negócio intrínsecas ao projeto de uma memória cache.
     */
    private void validarRequest(SimulacaoRequest request) {
        if (!isPotenciaDeDois(request.tamanhoCacheBytes())) {
            throw new RegraNegocioException("Tamanho da cache deve ser uma potência de 2");
        }
        if (!isPotenciaDeDois(request.tamanhoBlocoBytes())) {
            throw new RegraNegocioException("Tamanho do bloco deve ser uma potência de 2");
        }
        if (request.tamanhoBlocoBytes() > request.tamanhoCacheBytes()) {
            throw new RegraNegocioException("Tamanho do bloco não pode ser maior que o tamanho da cache");
        }

        int totalLinhas = request.tamanhoCacheBytes() / request.tamanhoBlocoBytes();

        if (request.mapeamento() == TipoMapeamento.CONJUNTO_ASSOCIATIVO) {
            if (request.numeroVias() == null || request.numeroVias() < 1 || !isPotenciaDeDois(request.numeroVias())) {
                throw new RegraNegocioException("Número de vias deve ser uma potência de 2 positiva");
            }
            if (request.numeroVias() > totalLinhas) {
                throw new RegraNegocioException("Número de vias não pode exceder o total de linhas da cache");
            }
        }

        if (request.mapeamento() != TipoMapeamento.DIRETO && request.substituicao() == null) {
            throw new RegraNegocioException("Política de substituição é obrigatória para mapeamentos associativos");
        }
    }

    private List<LinhaCacheInterna> inicializarLinhas(int totalLinhas, int totalConjuntos, int numeroVias) {
        List<LinhaCacheInterna> linhas = new ArrayList<>(totalLinhas);
        for (int i = 0; i < totalLinhas; i++) {
            Integer conjuntoIndex = totalConjuntos > 1 ? i / numeroVias : (totalConjuntos == 1 ? 0 : null);
            linhas.add(new LinhaCacheInterna(i, conjuntoIndex));
        }
        return linhas;
    }

    private List<LinhaCacheInterna> buscarLinhasDoConjunto(List<LinhaCacheInterna> linhas, Integer indice, TipoMapeamento mapeamento, int totalLinhas, int numeroVias) {
        if (mapeamento == TipoMapeamento.DIRETO) {
            return List.of(linhas.get(indice));
        } else if (mapeamento == TipoMapeamento.TOTALMENTE_ASSOCIATIVO) {
            return linhas;
        } else {
            int inicio = indice * numeroVias;
            int fim = inicio + numeroVias;
            return linhas.subList(inicio, fim); // SubList opera como view (O(1) memória e alocação leve)
        }
    }

    /**
     * Valida se um número inteiro positivo é estritamente uma potência de 2 utilizando 
     * a propriedade de que N & (N - 1) será 0 para potências perfeitas.
     */
    private boolean isPotenciaDeDois(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    /**
     * Calcula o logaritmo na base 2 de um número utilizando o deslocamento de bits em zero.
     * É extremamente rápido e evita o casting flutuante de Math.log().
     */
    private int log2(int n) {
        return Integer.numberOfTrailingZeros(n);
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    /**
     * Classe interna estática responsável por reter o estado mutável das linhas/quadros 
     * da cache isolado dentro do escopo de execução da requisição stateless.
     */
    private static class LinhaCacheInterna {
        final int indiceLinha;
        final Integer conjuntoIndex;
        boolean valida = false;
        int tag = 0;
        long ultimaUtilizacao = 0;
        long ordemChegada = 0;

        LinhaCacheInterna(int indiceLinha, Integer conjuntoIndex) {
            this.indiceLinha = indiceLinha;
            this.conjuntoIndex = conjuntoIndex;
        }
    }
}
