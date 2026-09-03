package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.resposta.XpService;
import br.com.cachly.backend.simulador.PoliticaSubstituicao;
import br.com.cachly.backend.simulador.SimulacaoRequest;
import br.com.cachly.backend.simulador.SimulacaoResponse;
import br.com.cachly.backend.simulador.SimuladorCacheService;
import br.com.cachly.backend.simulador.TipoMapeamento;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DesafioCacheService {

    private final SimuladorCacheService simuladorCacheService;
    private final UsuarioRepository usuarioRepository;
    private final XpService xpService;

    private static record DesafioInterno(
            String id,
            String titulo,
            String descricao,
            DificuldadeQuestao dificuldade,
            int xpRecompensa,
            SimulacaoRequest configuracao,
            String pergunta,
            List<OpcaoDesafioResponse> opcoes,
            String opcaoCorretaId,
            String dica,
            String explicacao
    ) {
        public DesafioCacheResponse paraResponse() {
            return new DesafioCacheResponse(
                    id,
                    titulo,
                    descricao,
                    dificuldade,
                    xpRecompensa,
                    configuracao,
                    pergunta,
                    opcoes,
                    dica
            );
        }
    }

    private static final Map<String, DesafioInterno> DESAFIOS;

    static {
        Map<String, DesafioInterno> mapa = new LinkedHashMap<>();

        // Desafio 1: Miss Compulsório (Cold Start)
        mapa.put("cold-miss", new DesafioInterno(
                "cold-miss",
                "Miss Compulsório (Cold Start)",
                "Quando a memória cache é inicializada, ela se encontra vazia (todas as linhas com bit de validade = 0). Nessa fase, o primeiro acesso a qualquer bloco gera obrigatoriamente uma falta compulsória.",
                DificuldadeQuestao.FACIL,
                30,
                new SimulacaoRequest(64, 16, null, TipoMapeamento.DIRETO, PoliticaSubstituicao.LRU, List.of(0, 16, 32, 48)),
                "Na primeira rodada de leitura desses 4 blocos de memória distintos, quantos misses e hits serão registrados?",
                List.of(
                        new OpcaoDesafioResponse("A", "4 Misses e 0 Hits (todos os blocos precisam ser carregados da memória principal)"),
                        new OpcaoDesafioResponse("B", "2 Misses e 2 Hits (a localidade espacial antecipa os blocos pares)"),
                        new OpcaoDesafioResponse("C", "0 Misses e 4 Hits (a memória cache já inicia pré-carregada pelo barramento)"),
                        new OpcaoDesafioResponse("D", "3 Misses e 1 Hit (apenas a última linha sofre descarte por colisão)")
                ),
                "A",
                "Lembre-se: no estado inicial, o processador ainda não trouxe nenhum bloco para as linhas da cache.",
                "Correto! Na primeira vez em que um bloco é referenciado após o boot da máquina, ocorre um Miss Compulsório (ou Cold Miss). Como os endereços 0, 16, 32 e 48 pertencem a blocos distintos (0, 1, 2 e 3), todos resultam em falta."
        ));

        // Desafio 2: O Fenômeno do Thrashing no Mapeamento Direto
        mapa.put("thrashing-direto", new DesafioInterno(
                "thrashing-direto",
                "O Fenômeno do Thrashing no Mapeamento Direto",
                "No mapeamento direto, a função modular (Endereço do Bloco % Número de Linhas) define uma única linha possível para cada bloco. Observe o que ocorre quando dois endereços competem pela mesma linha da cache.",
                DificuldadeQuestao.MEDIO,
                50,
                new SimulacaoRequest(64, 16, null, TipoMapeamento.DIRETO, PoliticaSubstituicao.LRU, List.of(0, 64, 0, 64, 0, 64)),
                "Mesmo repetindo a sequência de acessos 3 vezes consecutivas, por que a taxa de acerto é 0%?",
                List.of(
                        new OpcaoDesafioResponse("A", "Porque ocorre conflito de linha (Thrashing): o bloco 4 expulsa o bloco 0 e vice-versa continuamente"),
                        new OpcaoDesafioResponse("B", "Porque o tamanho de bloco de 16 bytes é insuficiente para a arquitetura"),
                        new OpcaoDesafioResponse("C", "Porque a política LRU é incompatível com o mapeamento direto"),
                        new OpcaoDesafioResponse("D", "Porque a capacidade total da cache é excedida em todas as suas linhas")
                ),
                "A",
                "Calcule o índice de linha para o Bloco 0 (endereço 0) e para o Bloco 4 (endereço 64).",
                "Perfeito! Em uma cache com 4 linhas, o Bloco 0 (0 / 16 = 0 % 4 = 0) e o Bloco 4 (64 / 16 = 4 % 4 = 0) são mapeados para a Linha 0. A cada acesso alternado, um bloco substitui o outro antes que possa ser reutilizado, gerando 100% de faltas por conflito (Thrashing)."
        ));

        // Desafio 3: A Solução com Associatividade por Conjunto (2-Way)
        mapa.put("solucao-associativa", new DesafioInterno(
                "solucao-associativa",
                "Eliminando o Conflito com Associatividade (2-Way)",
                "Para solucionar o problema de conflito visto no desafio anterior sem o alto custo de hardware de um totalmente associativo, organizamos as linhas em conjuntos com 2 vias (2-Way Set Associative).",
                DificuldadeQuestao.MEDIO,
                60,
                new SimulacaoRequest(64, 16, 2, TipoMapeamento.CONJUNTO_ASSOCIATIVO, PoliticaSubstituicao.LRU, List.of(0, 64, 0, 64, 0, 64)),
                "Executando a mesma sequência [0, 64, 0, 64, 0, 64], quantos HITS são obtidos agora na cache associativa por conjunto?",
                List.of(
                        new OpcaoDesafioResponse("A", "4 Hits (os dois primeiros acessos são misses de aquecimento e os quatro seguintes são hits)"),
                        new OpcaoDesafioResponse("B", "6 Hits (todas as requisições encontram os blocos em cache)"),
                        new OpcaoDesafioResponse("C", "0 Hits (o conflito persiste pois ambos ainda mapeiam para o conjunto 0)"),
                        new OpcaoDesafioResponse("D", "2 Hits (apenas o primeiro bloco consegue ser preservado)")
                ),
                "A",
                "Agora o Conjunto 0 possui 2 linhas (vias). O Bloco 0 e o Bloco 4 cabem simultaneamente no mesmo conjunto?",
                "Excelente dedução! Com 2 vias no Conjunto 0, o Bloco 0 reside na Via 0 e o Bloco 4 ocupa a Via 1. Ambos permanecem na cache juntos, eliminando os misses por conflito e convertendo todas as 4 repetições seguintes em HITS."
        ));

        // Desafio 4: Duelo de Políticas - Localidade Temporal com LRU
        mapa.put("lru-vs-fifo", new DesafioInterno(
                "lru-vs-fifo",
                "Duelo de Políticas: Localidade Temporal com LRU",
                "Quando uma cache totalmente associativa fica cheia, a política de substituição precisa escolher qual bloco descartar. O algoritmo LRU (Least Recently Used) descarta o bloco que está há mais tempo sem sofrer acesso.",
                DificuldadeQuestao.DIFICIL,
                80,
                new SimulacaoRequest(64, 16, null, TipoMapeamento.TOTALMENTE_ASSOCIATIVO, PoliticaSubstituicao.LRU, List.of(0, 16, 32, 48, 0, 80)),
                "Ao solicitar o endereço 80 (bloco 5) no último passo, qual bloco é substituído da cache pela política LRU?",
                List.of(
                        new OpcaoDesafioResponse("A", "Bloco 1 (endereço 16), pois o Bloco 0 foi reutilizado no passo 5 e teve sua prioridade renovada"),
                        new OpcaoDesafioResponse("B", "Bloco 0 (endereço 0), pois foi o primeiro a ser inserido na cache"),
                        new OpcaoDesafioResponse("C", "Bloco 3 (endereço 48), por se encontrar na última linha física"),
                        new OpcaoDesafioResponse("D", "Nenhum bloco é substituído porque a memória cache expande dinamicamente")
                ),
                "A",
                "Observe atentamente o passo 5: o Bloco 0 foi acessado novamente logo antes do Bloco 5 chegar!",
                "Brilhante! O Bloco 0 entrou primeiro, mas seu acesso no passo 5 atualizou sua data de utilização para o tempo mais recente. Quando o Bloco 5 requisitou espaço no passo 6, o bloco com menor utilização recente era o Bloco 1 (acessado no passo 2), sendo ele a vítima correta do LRU."
        ));

        DESAFIOS = Collections.unmodifiableMap(mapa);
    }

    public List<DesafioCacheResponse> listarDesafios() {
        return DESAFIOS.values().stream()
                .map(DesafioInterno::paraResponse)
                .toList();
    }

    public DesafioCacheResponse obterDesafio(String id) {
        DesafioInterno desafio = buscarDesafioOuFalhar(id);
        return desafio.paraResponse();
    }

    @Transactional
    public ResultadoDesafioResponse verificarDesafio(String id, VerificarDesafioRequest request, Usuario usuario) {
        DesafioInterno desafio = buscarDesafioOuFalhar(id);

        boolean correto = desafio.opcaoCorretaId().equalsIgnoreCase(request.opcaoSelecionadaId().trim());
        int xpGanho = 0;

        if (correto && usuario != null) {
            xpGanho = desafio.xpRecompensa();
            int xpAtual = usuario.getXpTotal() != null ? usuario.getXpTotal() : 0;
            int novoXp = xpAtual + xpGanho;
            usuario.setXpTotal(novoXp);
            int novoNivel = xpService.calcularNivel(novoXp);
            usuario.setNivel(novoNivel);
            usuarioRepository.save(usuario);
        }

        SimulacaoResponse simulacao = simuladorCacheService.executarSimulacao(desafio.configuracao());

        Integer nivelAtual = (usuario != null && usuario.getNivel() != null) ? usuario.getNivel() : 1;
        String nomeNivel = xpService.nomeDoNivel(nivelAtual);
        Integer xpTotalAtual = (usuario != null && usuario.getXpTotal() != null) ? usuario.getXpTotal() : 0;

        return new ResultadoDesafioResponse(
                correto,
                desafio.opcaoCorretaId(),
                correto ? desafio.explicacao() : "Não foi dessa vez! Dica: " + desafio.dica(),
                xpGanho,
                simulacao,
                nivelAtual,
                nomeNivel,
                xpTotalAtual
        );
    }

    private DesafioInterno buscarDesafioOuFalhar(String id) {
        DesafioInterno desafio = DESAFIOS.get(id);
        if (desafio == null) {
            throw new RecursoNaoEncontradoException("Desafio não encontrado com o identificador: " + id);
        }
        return desafio;
    }
}
