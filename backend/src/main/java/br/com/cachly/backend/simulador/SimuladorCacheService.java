package br.com.cachly.backend.simulador;

import br.com.cachly.backend.comum.erro.RegraNegocioException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Serviço responsável por executar a simulação didática da memória cache.
 * 
 * Design Arquintetural: Esta classe foi desenhada para ser 100% stateless.
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

        TopologiaCacheStrategy topologiaStrategy = resolverStrategy(mapeamento);
        int numeroVias = topologiaStrategy.calcularVias(totalLinhas, request);
        int totalConjuntos = topologiaStrategy.calcularConjuntos(totalLinhas, numeroVias);
        int bitsIndice = topologiaStrategy.calcularBitsIndice(totalConjuntos);

        // Cálculo da divisão de bits do endereço
        // Utilizamos logaritmo na base 2, visto que os tamanhos obrigatoriamente são potências de 2.
        int bitsOffset = log2(tamanhoBloco);
        
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

            int offsetMask = (1 << bitsOffset) - 1;
            int offset = endereco & offsetMask;

            Integer indiceObj = bitsIndice == 0 ? null : (endereco >>> bitsOffset) & ((1 << bitsIndice) - 1);
            int tag = endereco >>> (bitsOffset + bitsIndice);

            // Reduz o escopo de busca apenas para as linhas pertencentes ao conjunto calculado (ou todas se for totalmente associativo)
            int indice = indiceObj != null ? indiceObj : 0;
            List<LinhaCacheInterna> candidatoLinhas = topologiaStrategy.buscarLinhasDoConjunto(linhas, indice, numeroVias);

            // Verifica se o bloco correspondente à tag já está carregado na memória cache
            LinhaCacheInterna linhaHit = candidatoLinhas.stream()
                    .filter(l -> l.valida && l.tag == tag)
                    .findFirst()
                    .orElse(null);

            boolean isHit = linhaHit != null;
            Integer blocoSubstituido = null;
            EstadoLinhaCacheResponse deltaLinha = null;
            
            StringBuilder explicacao = new StringBuilder();
            explicacao.append(String.format("Passo %d: O processador solicitou o Endereço %d. ", passoNumero, endereco));
            explicacao.append(String.format("Ao decodificar, encontramos Tag = %d", tag));
            if (indiceObj != null) {
                explicacao.append(String.format(", Índice = %d", indiceObj));
            }
            explicacao.append(String.format(" e Offset = %d. ", offset));
            
            if (indiceObj != null) {
                explicacao.append(String.format("Procurando pela Tag %d dentro do Conjunto %d... ", tag, indiceObj));
            } else {
                explicacao.append(String.format("Procurando pela Tag %d em toda a cache (Totalmente Associativo)... ", tag));
            }

            if (isHit) {
                totalHits++;
                linhaHit.ultimaUtilizacao = tempoGlobal++;
                explicacao.append(String.format("HIT! Encontramos a Tag %d na Linha %d! O acesso foi super rápido pois o bloco inteiro (com o offset %d) já estava na cache.", tag, linhaHit.indiceLinha, offset));
                if (substituicao == PoliticaSubstituicao.LRU) {
                    explicacao.append(String.format(" Pela política LRU, a Linha %d foi atualizada como a 'mais recentemente usada'.", linhaHit.indiceLinha));
                }
            } else {
                totalMisses++;
                
                LinhaCacheInterna linhaVazia = candidatoLinhas.stream()
                        .filter(l -> !l.valida)
                        .findFirst()
                        .orElse(null);

                LinhaCacheInterna linhaAlvo;
                if (linhaVazia != null) {
                    linhaAlvo = linhaVazia;
                    explicacao.append(String.format("MISS! A Tag %d não foi encontrada. Fomos buscar na RAM e trouxemos o bloco para a Linha %d, que estava VAZIA. Esse é um 'Miss Compulsório' (inevitável no primeiro acesso ao bloco).", tag, linhaAlvo.indiceLinha));
                } else {
                    if (substituicao == PoliticaSubstituicao.LRU) {
                        linhaAlvo = candidatoLinhas.stream()
                                .min(Comparator.comparingLong(l -> l.ultimaUtilizacao))
                                .orElseThrow();
                        explicacao.append(String.format("MISS! A Tag %d não estava na cache e o conjunto estava CHEIO. A política LRU escolheu evictar a Linha %d (Tag antiga %d) por ser a menos usada recentemente. O novo bloco tomou seu lugar.", tag, linhaAlvo.indiceLinha, linhaAlvo.tag));
                    } else { 
                        linhaAlvo = candidatoLinhas.stream()
                                .min(Comparator.comparingLong(l -> l.ordemChegada))
                                .orElseThrow();
                        explicacao.append(String.format("MISS! A Tag %d não estava na cache e o conjunto estava CHEIO. A política FIFO evictou a Linha %d (Tag antiga %d) por ser a mais antiga a ter entrado. O novo bloco tomou seu lugar.", tag, linhaAlvo.indiceLinha, linhaAlvo.tag));
                    }
                }

                // Efetua a carga do novo bloco na linha alvo
                linhaAlvo.valida = true;
                linhaAlvo.tag = tag;
                linhaAlvo.ordemChegada = tempoGlobal;
                linhaAlvo.ultimaUtilizacao = tempoGlobal++;
                blocoSubstituido = linhaAlvo.indiceLinha;
                
                deltaLinha = new EstadoLinhaCacheResponse(
                        linhaAlvo.indiceLinha,
                        linhaAlvo.conjuntoIndex,
                        linhaAlvo.valida,
                        linhaAlvo.tag
                );
            }

            passos.add(new PassoSimulacaoResponse(
                    passoNumero,
                    endereco,
                    tag,
                    indiceObj,
                    offset,
                    isHit,
                    blocoSubstituido,
                    deltaLinha,
                    explicacao.toString()
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

    private TopologiaCacheStrategy resolverStrategy(TipoMapeamento mapeamento) {
        return switch (mapeamento) {
            case DIRETO -> new MapeamentoDiretoStrategy();
            case TOTALMENTE_ASSOCIATIVO -> new TotalmenteAssociativoStrategy();
            case CONJUNTO_ASSOCIATIVO -> new ConjuntoAssociativoStrategy();
        };
    }

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

    private interface TopologiaCacheStrategy {
        int calcularVias(int totalLinhas, SimulacaoRequest request);
        int calcularConjuntos(int totalLinhas, int vias);
        int calcularBitsIndice(int totalConjuntos);
        List<LinhaCacheInterna> buscarLinhasDoConjunto(List<LinhaCacheInterna> linhas, int indice, int vias);
    }

    private class MapeamentoDiretoStrategy implements TopologiaCacheStrategy {
        public int calcularVias(int totalLinhas, SimulacaoRequest request) { return 1; }
        public int calcularConjuntos(int totalLinhas, int vias) { return totalLinhas; }
        public int calcularBitsIndice(int totalConjuntos) { return log2(totalConjuntos); }
        public List<LinhaCacheInterna> buscarLinhasDoConjunto(List<LinhaCacheInterna> linhas, int indice, int vias) {
            return List.of(linhas.get(indice));
        }
    }

    private class TotalmenteAssociativoStrategy implements TopologiaCacheStrategy {
        public int calcularVias(int totalLinhas, SimulacaoRequest request) { return totalLinhas; }
        public int calcularConjuntos(int totalLinhas, int vias) { return 1; }
        public int calcularBitsIndice(int totalConjuntos) { return 0; }
        public List<LinhaCacheInterna> buscarLinhasDoConjunto(List<LinhaCacheInterna> linhas, int indice, int vias) {
            return linhas;
        }
    }

    private class ConjuntoAssociativoStrategy implements TopologiaCacheStrategy {
        public int calcularVias(int totalLinhas, SimulacaoRequest request) { return request.numeroVias(); }
        public int calcularConjuntos(int totalLinhas, int vias) { return totalLinhas / vias; }
        public int calcularBitsIndice(int totalConjuntos) { return log2(totalConjuntos); }
        public List<LinhaCacheInterna> buscarLinhasDoConjunto(List<LinhaCacheInterna> linhas, int indice, int vias) {
            int inicio = indice * vias;
            return linhas.subList(inicio, inicio + vias);
        }
    }
}
