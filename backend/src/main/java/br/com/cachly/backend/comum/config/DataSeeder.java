package br.com.cachly.backend.comum.config;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.categoria.Categoria;
import br.com.cachly.backend.categoria.CategoriaRepository;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import br.com.cachly.backend.questao.comentario.Comentario;
import br.com.cachly.backend.questao.comentario.ComentarioRepository;
import br.com.cachly.backend.resposta.TentativaQuestao;
import br.com.cachly.backend.resposta.TentativaQuestaoRepository;
import br.com.cachly.backend.resposta.XpService;
import br.com.cachly.backend.simulador.desafio.DesafioConcluido;
import br.com.cachly.backend.simulador.desafio.DesafioConcluidoRepository;
import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import br.com.cachly.backend.usuario.aluno.RankingSemanalHistorico;
import br.com.cachly.backend.usuario.aluno.RankingSemanalHistoricoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente de inicialização de dados pedagógicos de demonstração (Seed).
 *
 * NOTA: Todos os dados gerados aqui são FICTÍCIOS para fins de demonstração acadêmica e testes manuais.
 * Este seeder é executado exclusivamente em perfis que NÃO sejam de teste (@Profile("!test")).
 * O seeder só popula bancos vazios. Em reinicializações e deploys posteriores, os dados
 * existentes são preservados integralmente.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final QuestaoRepository questaoRepository;
    private final ComentarioRepository comentarioRepository;
    private final TentativaQuestaoRepository tentativaQuestaoRepository;
    private final RankingSemanalHistoricoRepository rankingSemanalHistoricoRepository;
    private final DesafioConcluidoRepository desafioConcluidoRepository;
    private final br.com.cachly.backend.simulador.desafio.DesafioSimuladorRepository desafioSimuladorRepository;
    private final PasswordEncoder passwordEncoder;
    private final XpService xpService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (bancoJaPossuiDados()) {
            if (desafioSimuladorRepository.count() == 0) {
                criarDesafiosSimulador();
            }
            return;
        }

        // =========================================================================
        // 1. USUÁRIOS E PERFIS
        // =========================================================================

        // Administrador do Sistema (para testar RBAC e rotas de gestão de conteúdo)
        Usuario admin = criarUsuario(
                "Administrador Cachly",
                "admin@cachly.com",
                "admin123",
                PerfilUsuario.ADMINISTRADOR,
                0,
                0,
                0,
                null
        );

        // Aluno Principal de Demonstração (com histórico rico e consistente)
        // 205 XP de questões + 80 XP de desafios = 285 XP (Nível 3 via XpService)
        Usuario aluno = criarUsuario(
                "Aluno de Teste",
                "aluno@teste.com",
                "senha123",
                PerfilUsuario.ALUNO,
                285,
                180,
                5,
                LocalDate.now()
        );

        // Alunos Concorrentes para a Liga Semanal e Ranking
        Usuario beatriz = criarUsuario(
                "Beatriz Mendes",
                "beatriz@teste.com",
                "senha123",
                PerfilUsuario.ALUNO,
                520,
                260,
                8,
                LocalDate.now()
        );

        Usuario carlos = criarUsuario(
                "Carlos Eduardo",
                "carlos@teste.com",
                "senha123",
                PerfilUsuario.ALUNO,
                340,
                140,
                4,
                LocalDate.now()
        );

        Usuario daniela = criarUsuario(
                "Daniela Rocha",
                "daniela@teste.com",
                "senha123",
                PerfilUsuario.ALUNO,
                160,
                70,
                2,
                LocalDate.now().minusDays(1)
        );

        // =========================================================================
        // 2. CATEGORIAS E QUESTÕES DIDÁTICAS (12 QUESTÕES: FÁCIL, MÉDIO, DIFÍCIL)
        // =========================================================================

        // --- Categoria 1: Mapeamento Direto ---
        Categoria catDireto = salvarCategoria(
                "Mapeamento Direto",
                "Mapeamento onde cada bloco da memória principal é mapeado para uma única linha específica da cache."
        );

        Questao q1 = criarQuestao(catDireto,
                "Qual é a principal característica do mapeamento direto de cache?",
                DificuldadeQuestao.FACIL, 10,
                "No mapeamento direto, a função modular (Endereço do Bloco % Número de Linhas) determina uma única posição exata na cache onde o bloco pode residir. Isso torna a busca rápida e simples em hardware, mas suscetível a colisões.",
                "Cada bloco da memória principal é mapeado para exatamente uma linha específica na cache.",
                List.of(
                        "Qualquer bloco da memória principal pode ser armazenado em qualquer linha livre da cache.",
                        "A memória cache é dividida em conjuntos de vias, e o bloco pode ocupar qualquer via de um conjunto.",
                        "Os blocos são alocados sequencialmente na cache sem função de mapeamento prévia."
                ),
                1
        );

        Questao q2 = criarQuestao(catDireto,
                "Como o endereço de memória emitido pelo processador é dividido no mapeamento direto?",
                DificuldadeQuestao.MEDIO, 15,
                "O endereço é decomposto em três campos contíguos: o Deslocamento (Offset) para indexar o byte no bloco, o Índice (Index) para selecionar a linha unívoca da cache, e a Tag para validar se o bloco presente é o bloco requisitado.",
                "Tag (identificador de bloco), Índice (seleção da linha) e Deslocamento/Offset (byte dentro do bloco).",
                List.of(
                        "Tag e Deslocamento apenas, dispensando o campo de Índice.",
                        "Conjunto (Set), Via e Deslocamento (Offset).",
                        "Página virtual, Quadro físico e Deslocamento (Offset)."
                ),
                3
        );

        Questao q3 = criarQuestao(catDireto,
                "O que caracteriza o fenômeno de Thrashing (conflito contínuo) em uma memória cache com mapeamento direto?",
                DificuldadeQuestao.DIFICIL, 25,
                "Thrashing ocorre quando múltiplos blocos acessados frequentemente competem pela mesma linha da cache (possuem o mesmo índice, mas tags distintas). A cada acesso alternado, um bloco expulsa o outro compulsoriamente, gerando misses contínuos mesmo com outras linhas da cache vazias.",
                "Ocorre quando múltiplos blocos com o mesmo índice competem pela mesma linha, causando evicções repetidas mesmo com linhas livres na cache.",
                List.of(
                        "Ocorre quando todas as linhas da cache estão cheias e a capacidade total é saturada.",
                        "Ocorre quando a memória principal opera em velocidade maior que a cache, congestionando o barramento.",
                        "Ocorre quando o algoritmo LRU falha na predição do próximo endereço acessado."
                ),
                2
        );

        // --- Categoria 2: Totalmente Associativo ---
        Categoria catTotalmente = salvarCategoria(
                "Totalmente Associativo",
                "Mapeamento onde qualquer bloco da principal pode ser alocado em qualquer linha disponível da cache."
        );

        Questao q4 = criarQuestao(catTotalmente,
                "Qual é a maior vantagem do mapeamento totalmente associativo em comparação ao mapeamento direto?",
                DificuldadeQuestao.FACIL, 10,
                "No mapeamento totalmente associativo, um bloco da memória principal pode residir em qualquer linha da cache. Portanto, misses de conflito entre blocos não ocorrem; uma substituição só acontece quando toda a capacidade da cache é atingida (miss de capacidade).",
                "Elimina totalmente as faltas por conflito de linha, permitindo alocar blocos em qualquer linha vaga da cache.",
                List.of(
                        "Possui circuito de hardware muito mais simples e barato de construir do que o mapeamento direto.",
                        "O tempo de acesso (hit time) é menor devido à ausência de comparadores de tag.",
                        "Não requer nenhuma política de substituição (como LRU ou FIFO) quando a cache fica cheia."
                ),
                4
        );

        Questao q5 = criarQuestao(catTotalmente,
                "Como o endereço de memória é interpretado no mapeamento totalmente associativo?",
                DificuldadeQuestao.MEDIO, 15,
                "Como não existe vinculação fixa entre blocos e linhas específicas, não há campo de Índice. O endereço é dividido exclusivamente em Tag (para comparação simultânea contra todas as linhas) e Offset (para indicar o byte desejado no bloco).",
                "Apenas Tag e Deslocamento (Offset), dispensando o campo de Índice.",
                List.of(
                        "Tag, Índice e Deslocamento, onde o índice aponta o bloco na memória principal.",
                        "Índice e Offset apenas, pois a tag é desnecessária quando a busca é total.",
                        "Número da Via, Tag e Offset de byte."
                ),
                2
        );

        Questao q6 = criarQuestao(catTotalmente,
                "Por que caches totalmente associativas geralmente são restritas a tamanhos reduzidos em hardware?",
                DificuldadeQuestao.DIFICIL, 25,
                "Para verificar um Hit sem penalidade de latência, o circuito precisa comparar a Tag do endereço contra as Tags de TODAS as linhas em paralelo. Isso requer um comparador de hardware por linha, tornando o custo, a área de silício e o consumo de energia proibitivos para caches de alta capacidade.",
                "Porque exigem um comparador de hardware por linha para realizar a busca de Tag em paralelo, elevando custo e consumo.",
                List.of(
                        "Porque a taxa de acertos do totalmente associativo decresce conforme o número de linhas aumenta.",
                        "Porque o protocolo de coerência MESI é matematicamente incompatível com mapeamento totalmente associativo.",
                        "Porque não é possível calcular os bits de deslocamento quando a cache possui mais de 64 linhas."
                ),
                1
        );

        // --- Categoria 3: Associativo por Conjunto ---
        Categoria catConjunto = salvarCategoria(
                "Associativo por Conjunto",
                "Mapeamento híbrido que agrupa as linhas em conjuntos de N vias (N-way set associative)."
        );

        Questao q7 = criarQuestao(catConjunto,
                "O que define uma organização de cache associativa por conjunto N-way (N-way Set-Associative)?",
                DificuldadeQuestao.FACIL, 10,
                "A cache é subdividida em múltiplos conjuntos, onde cada conjunto possui exatamente N linhas (vias). O endereço seleciona um conjunto específico (via índice), e dentro desse conjunto o bloco pode ser posicionado em qualquer uma das N vias.",
                "A cache é dividida em conjuntos, e cada bloco mapeia para um conjunto específico, podendo ocupar qualquer uma das N vias.",
                List.of(
                        "Cada bloco da RAM mapeia para N posições fixas e contíguas de memória principal.",
                        "A cache possui N linhas no total, funcionando como uma fila circular FIFO.",
                        "O processador executa N acessos em paralelo antes de consultar a memória principal."
                ),
                3
        );

        Questao q8 = criarQuestao(catConjunto,
                "Em uma cache com 64 linhas organizada em 4 vias (4-way set associative) e blocos de 16 bytes, quantos conjuntos existem e quantos bits são usados para o Índice?",
                DificuldadeQuestao.MEDIO, 15,
                "Total de conjuntos = Total de linhas / Vias = 64 / 4 = 16 conjuntos. Para indexar 16 conjuntos, são necessários log2(16) = 4 bits de Índice.",
                "16 conjuntos e 4 bits de Índice.",
                List.of(
                        "64 conjuntos e 6 bits de Índice.",
                        "4 conjuntos e 2 bits de Índice.",
                        "32 conjuntos e 5 bits de Índice."
                ),
                4
        );

        Questao q9 = criarQuestao(catConjunto,
                "Ao aumentar a associatividade de uma cache de 2-way para 8-way, qual trade-off clássico de arquitetura é observado?",
                DificuldadeQuestao.DIFICIL, 25,
                "Aumentar o número de vias diminui as chances de misses por conflito. Contudo, adiciona comparadores e multiplexadores na saída, o que pode aumentar o tempo de acerto (hit time) e elevar o consumo energético.",
                "Reduz as faltas por conflito, mas pode aumentar o tempo de acerto (hit time) e o consumo de energia.",
                List.of(
                        "Elimina os misses compulsórios, porém duplica o tamanho do campo de Offset.",
                        "Diminui o tamanho da Tag, mas inviabiliza o uso de algoritmos de substituição.",
                        "Aumenta a taxa de misses por capacidade, embora simplifique o circuito."
                ),
                2
        );

        // --- Categoria 4: Políticas de Substituição ---
        Categoria catSubstituicao = salvarCategoria(
                "Políticas de Substituição",
                "Algoritmos para escolha de linha/bloco a evictar quando o conjunto está cheio (LRU, FIFO, LFU)."
        );

        Questao q10 = criarQuestao(catSubstituicao,
                "Qual princípio de localidade a política de substituição LRU (Least Recently Used) explora primordialmente?",
                DificuldadeQuestao.FACIL, 10,
                "O LRU explora a Localidade Temporal: assume que dados acessados recentemente tendem a ser requisitados novamente no futuro próximo. Assim, substitui o bloco que está há mais tempo sem sofrer acesso.",
                "Localidade Temporal, descartando o bloco cujo último acesso ocorreu há mais tempo.",
                List.of(
                        "Localidade Espacial, descartando o bloco com endereço mais distante no barramento.",
                        "Localidade Sequencial, descartando o bloco que foi inserido primeiro na cache.",
                        "Localidade Aleatória, escolhendo uma linha através de gerador pseudo-randômico."
                ),
                1
        );

        Questao q11 = criarQuestao(catSubstituicao,
                "Como opera a política de substituição FIFO (First-In, First-Out) em uma cache associativa?",
                DificuldadeQuestao.MEDIO, 15,
                "O FIFO mantém estritamente a ordem cronológica de carregamento do bloco na cache. Quando uma substituição é exigida, o bloco mais antigo presente é evictado, independentemente de quando ou quantas vezes foi lido recentemente.",
                "Substitui sempre o bloco que está presente há mais tempo na cache, ignorando acessos recentes.",
                List.of(
                        "Substitui o bloco que teve o menor número total de acessos desde a inicialização.",
                        "Substitui o bloco que causou o maior número de colisões de barramento.",
                        "Atualiza a prioridade a cada hit, protegendo blocos acessados recentemente."
                ),
                4
        );

        Questao q12 = criarQuestao(catSubstituicao,
                "Por que a política LRU é considerada um 'algoritmo de pilha' (stack algorithm) imune à Anomalia de Belady, ao contrário do FIFO?",
                DificuldadeQuestao.DIFICIL, 25,
                "Em algoritmos de pilha como o LRU, o conjunto de blocos mantidos em uma cache de capacidade N é sempre um subconjunto estrito dos blocos mantidos em uma cache de capacidade N+1. Por isso, aumentar a capacidade nunca aumenta os misses no LRU, prevenindo a Anomalia de Belady observada no FIFO.",
                "Porque o conjunto de blocos em uma cache de capacidade N é sempre um subconjunto dos blocos em uma cache de capacidade N+1, impedindo aumento de misses.",
                List.of(
                        "Porque o LRU armazena todos os blocos na memória Stack do sistema operacional com ponteiro SP.",
                        "Porque o FIFO requer mais bits de hardware do que o LRU para armazenar timestamps.",
                        "Porque o LRU nunca realiza substituição em caso de hit na cache."
                ),
                3
        );

        // =========================================================================
        // 3. HISTÓRICO DE TENTATIVAS PARA ALUNO@TESTE.COM
        // Cenário para o Painel de Desempenho:
        // - Mapeamento Direto: 5 tentativas, 4 acertos (80% - alta)
        // - Totalmente Associativo: 5 tentativas, 4 acertos (80% - alta)
        // - Associativo por Conjunto: 5 tentativas, 3 acertos (60% - media)
        // - Políticas de Substituição: 5 tentativas, 1 acerto (20% - baixa -> RECOMENDADO!)
        // =========================================================================

        OffsetDateTime tempoBase = OffsetDateTime.now().minusDays(7);

        // Mapeamento Direto (4 acertos, 1 erro)
        gerarTentativasSimuladas(aluno, q1, 2, 1, tempoBase.plusDays(1), true);  // 1 acerto (10 XP), 1 erro
        gerarTentativasSimuladas(aluno, q2, 2, 2, tempoBase.plusDays(2), true);  // 2 acertos (30 XP na 1ª)
        gerarTentativasSimuladas(aluno, q3, 1, 1, tempoBase.plusDays(3), true);  // 1 acerto (75 XP)

        // Totalmente Associativo (4 acertos, 1 erro)
        gerarTentativasSimuladas(aluno, q4, 2, 2, tempoBase.plusDays(2), true);  // 2 acertos (10 XP na 1ª)
        gerarTentativasSimuladas(aluno, q5, 2, 2, tempoBase.plusDays(4), true);  // 2 acertos (30 XP na 1ª)
        gerarTentativasSimuladas(aluno, q6, 1, 0, tempoBase.plusDays(5), false); // 1 erro (0 XP)

        // Associativo por Conjunto (3 acertos, 2 erros)
        gerarTentativasSimuladas(aluno, q7, 2, 2, tempoBase.plusDays(3), true);  // 2 acertos (10 XP na 1ª)
        gerarTentativasSimuladas(aluno, q8, 2, 1, tempoBase.plusDays(5), true);  // 1 acerto (30 XP na 1ª), 1 erro
        gerarTentativasSimuladas(aluno, q9, 1, 0, tempoBase.plusDays(6), false); // 1 erro (0 XP)

        // Políticas de Substituição (1 acerto, 4 erros -> Categoria mais fraca)
        gerarTentativasSimuladas(aluno, q10, 2, 1, tempoBase.plusDays(4), true);  // 1 acerto (10 XP na 1ª), 1 erro
        gerarTentativasSimuladas(aluno, q11, 2, 0, tempoBase.plusDays(6), false); // 2 erros
        gerarTentativasSimuladas(aluno, q12, 1, 0, tempoBase.plusDays(7), false); // 1 erro

        // =========================================================================
        // 4. DESAFIOS DO SIMULADOR
        // Popula as entidades de desafio e conclui 2 desafios (80 XP total)
        // =========================================================================
        criarDesafiosSimulador();

        DesafioConcluido d1 = new DesafioConcluido();
        d1.setUsuario(aluno);
        d1.setDesafioId("cold-miss");
        d1.setDataConclusao(tempoBase.plusDays(2));
        desafioConcluidoRepository.save(d1);

        DesafioConcluido d2 = new DesafioConcluido();
        d2.setUsuario(aluno);
        d2.setDesafioId("thrashing-direto");
        d2.setDataConclusao(tempoBase.plusDays(4));
        desafioConcluidoRepository.save(d2);

        // =========================================================================
        // 5. HISTÓRICO DE RANKING SEMANAL (SNAPSHOT DA SEMANA ANTERIOR)
        // =========================================================================
        LocalDate segundaAnterior = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<RankingSemanalHistorico> snapshotAnterior = List.of(
                new RankingSemanalHistorico(aluno, 320, 1, segundaAnterior),
                new RankingSemanalHistorico(beatriz, 290, 2, segundaAnterior),
                new RankingSemanalHistorico(carlos, 180, 3, segundaAnterior),
                new RankingSemanalHistorico(daniela, 90, 4, segundaAnterior)
        );
        rankingSemanalHistoricoRepository.saveAll(snapshotAnterior);

        // =========================================================================
        // 6. FÓRUM / COMENTÁRIOS NAS QUESTÕES
        // =========================================================================

        // Comentários na Questão 1 (Mapeamento Direto)
        criarComentario(q1, carlos, "Alguém pode me explicar por que o mapeamento associativo seria pior aqui?", 2);
        criarComentario(q1, admin, "Excelente pergunta, Carlos! O mapeamento associativo não é necessariamente 'pior', mas exige comparadores em paralelo que aumentam o custo e podem atrasar um pouco o 'hit time'. Em sistemas embarcados simples, o mapeamento direto é muito mais eficiente em custo/benefício.", 15);
        criarComentario(q1, beatriz, "Nossa, ajudou muito! Eu estava confundindo o cálculo do Offset.", 5);

        // Comentários na Questão 3 (Thrashing)
        criarComentario(q3, daniela, "Esse negócio de Thrashing parece muito com paginação de SO, né?", 8);
        criarComentario(q3, beatriz, "Sim! Mas aqui acontece diretamente na cache L1/L2 porque dois blocos da RAM batem no mesmo índice. No SO seria a troca frenética de páginas entre RAM e HD.", 12);

        // Comentários na Questão 9 (Trade-off 2-way vs 8-way)
        criarComentario(q9, aluno, "Não entendi por que aumenta o consumo de energia...", 0);
        criarComentario(q9, admin, "Pensa o seguinte: numa cache 8-way, o processador precisa ligar 8 blocos da cache e comparar a Tag dos 8 ao mesmo tempo para ver se deu Hit. Essa ativação de 8 comparadores em paralelo consome bem mais energia do que comparar só 2.", 22);

        // =========================================================================
        // BANNER CONSOLE DE CONFIRMAÇÃO
        // =========================================================================
        System.out.println("=================================================================================");
        System.out.println("✅ [DataSeeder] Cenário acadêmico completo injetado com sucesso!");
        System.out.println("   👤 Administrador: admin@cachly.com / admin123 (Perfil: ADMINISTRADOR)");
        System.out.println("   🎓 Aluno Demo:     aluno@teste.com / senha123 (Nível " + aluno.getNivel() + " [" + xpService.nomeDoNivel(aluno.getNivel()) + "], " + aluno.getXpTotal() + " XP Total, " + aluno.getXpSemanal() + " XP Semanal)");
        System.out.println("   🏆 Liga Semanal:   4 alunos ativos disputando posições (Beatriz, Aluno, Carlos, Daniela)");
        System.out.println("   📊 Desempenho:     20 tentativas geradas (Políticas de Substituição com menor taxa para recomendação)");
        System.out.println("   🧩 Desafios:       2 concluídos (cold-miss, thrashing-direto) e 2 abertos para demo ao vivo");
        System.out.println("   📅 Histórico Liga: Snapshot de ranking salvo para " + segundaAnterior);
        System.out.println("=================================================================================");
    }

    private boolean bancoJaPossuiDados() {
        return usuarioRepository.count() > 0
                || categoriaRepository.count() > 0
                || questaoRepository.count() > 0
                || desafioSimuladorRepository.count() > 0;
    }

    private Usuario criarUsuario(String nome, String email, String senhaPlana, PerfilUsuario perfil,
                                 int xpTotal, int xpSemanal, int diasOfensiva, LocalDate dataOfensiva) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenhaHash(passwordEncoder.encode(senhaPlana));
        u.setPerfil(perfil);
        u.setAtivo(true);
        u.setXpTotal(xpTotal);
        u.setXpSemanal(xpSemanal);
        u.setNivel(xpService.calcularNivel(xpTotal));
        u.setDiasOfensiva(diasOfensiva);
        u.setDataUltimaOfensiva(dataOfensiva);
        return usuarioRepository.save(u);
    }

    private Categoria salvarCategoria(String nome, String descricao) {
        Categoria c = new Categoria();
        c.setNome(nome);
        c.setDescricao(descricao);
        c.setAtiva(true);
        return categoriaRepository.save(c);
    }

    private Questao criarQuestao(Categoria categoria, String enunciado, DificuldadeQuestao dificuldade,
                                 int xpBase, String explicacao, String alternativaCorretaTxt,
                                 List<String> alternativasErradasTxt, int posicaoCorreta) {
        Questao q = new Questao();
        q.setCategoria(categoria);
        q.setEnunciado(enunciado);
        q.setDificuldade(dificuldade);
        q.setXpBase(xpBase);
        q.setExplicacao(explicacao);
        q.setAtiva(true);

        List<Alternativa> lista = new ArrayList<>();
        int erradaIdx = 0;
        int totalAlternativas = alternativasErradasTxt.size() + 1;

        for (int i = 1; i <= totalAlternativas; i++) {
            Alternativa alt = new Alternativa();
            alt.setQuestao(q);
            alt.setAtiva(true);
            alt.setOrdem((short) i);

            if (i == posicaoCorreta) {
                alt.setTexto(alternativaCorretaTxt);
                alt.setCorreta(true);
            } else {
                alt.setTexto(alternativasErradasTxt.get(erradaIdx++));
                alt.setCorreta(false);
            }
            lista.add(alt);
        }

        q.setAlternativas(lista);
        return questaoRepository.save(q);
    }

    private void gerarTentativasSimuladas(Usuario usuario, Questao questao, int total, int acertos,
                                          OffsetDateTime dataBase, boolean acertouPrimeira) {
        Alternativa correta = questao.getAlternativas().stream()
                .filter(Alternativa::getCorreta)
                .findFirst()
                .orElseThrow();
        Alternativa errada = questao.getAlternativas().stream()
                .filter(a -> !a.getCorreta())
                .findFirst()
                .orElseThrow();

        int acertosRestantes = acertos;
        boolean primeiraTentativaRegistrada = false;

        for (int i = 0; i < total; i++) {
            TentativaQuestao t = new TentativaQuestao();
            t.setUsuario(usuario);
            t.setQuestao(questao);

            boolean ehCorreta;
            if (i == 0) {
                ehCorreta = acertouPrimeira;
                if (ehCorreta) acertosRestantes--;
            } else {
                ehCorreta = acertosRestantes > 0;
                if (ehCorreta) acertosRestantes--;
            }

            t.setAlternativa(ehCorreta ? correta : errada);
            t.setCorreta(ehCorreta);

            // Concede XP apenas se for correta e tiver sido a primeira vez que acertou
            int xpConcedido = 0;
            if (ehCorreta && !primeiraTentativaRegistrada) {
                xpConcedido = xpService.calcularXpGanho(questao);
                primeiraTentativaRegistrada = true;
            }
            t.setXpConcedido(xpConcedido);
            t.setRespondidaEm(dataBase.plusHours(i * 3L));

            tentativaQuestaoRepository.save(t);
        }
    }
    private void criarComentario(Questao questao, Usuario usuario, String conteudo, int upvotes) {
        Comentario c = new Comentario();
        c.setQuestao(questao);
        c.setUsuario(usuario);
        c.setConteudo(conteudo);
        c.setUpvotes(upvotes);
        comentarioRepository.save(c);
    }

    private void criarDesafiosSimulador() {
        br.com.cachly.backend.simulador.desafio.DesafioSimulador d1 = new br.com.cachly.backend.simulador.desafio.DesafioSimulador();
        d1.setId("cold-miss");
        d1.setTitulo("Miss Compulsório (Cold Start)");
        d1.setDescricao("Quando a memória cache é inicializada, ela se encontra vazia (todas as linhas com bit de validade = 0). Nessa fase, o primeiro acesso a qualquer bloco gera obrigatoriamente uma falta compulsória.");
        d1.setDificuldade(DificuldadeQuestao.FACIL);
        d1.setXpRecompensa(30);
        d1.setConfiguracao(new br.com.cachly.backend.simulador.SimulacaoRequest(64, 16, null, br.com.cachly.backend.simulador.TipoMapeamento.DIRETO, br.com.cachly.backend.simulador.PoliticaSubstituicao.LRU, List.of(0, 16, 32, 48)));
        d1.setPergunta("Na primeira rodada de leitura desses 4 blocos de memória distintos, quantos misses e hits serão registrados?");
        d1.setOpcoes(List.of(
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("A", "4 Misses e 0 Hits (todos os blocos precisam ser carregados da memória principal)"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("B", "2 Misses e 2 Hits (a localidade espacial antecipa os blocos pares)"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("C", "0 Misses e 4 Hits (a memória cache já inicia pré-carregada pelo barramento)"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("D", "3 Misses e 1 Hit (apenas a última linha sofre descarte por colisão)")
        ));
        d1.setOpcaoCorretaId("A");
        d1.setDica("Lembre-se: no estado inicial, o processador ainda não trouxe nenhum bloco para as linhas da cache.");
        d1.setExplicacao("Correto! Na primeira vez em que um bloco é referenciado após o boot da máquina, ocorre um Miss Compulsório (ou Cold Miss). Como os endereços 0, 16, 32 e 48 pertencem a blocos distintos (0, 1, 2 e 3), todos resultam em falta.");
        desafioSimuladorRepository.save(d1);

        br.com.cachly.backend.simulador.desafio.DesafioSimulador d2 = new br.com.cachly.backend.simulador.desafio.DesafioSimulador();
        d2.setId("thrashing-direto");
        d2.setTitulo("O Fenômeno do Thrashing no Mapeamento Direto");
        d2.setDescricao("No mapeamento direto, a função modular (Endereço do Bloco % Número de Linhas) define uma única linha possível para cada bloco. Observe o que ocorre quando dois endereços competem pela mesma linha da cache.");
        d2.setDificuldade(DificuldadeQuestao.MEDIO);
        d2.setXpRecompensa(50);
        d2.setConfiguracao(new br.com.cachly.backend.simulador.SimulacaoRequest(64, 16, null, br.com.cachly.backend.simulador.TipoMapeamento.DIRETO, br.com.cachly.backend.simulador.PoliticaSubstituicao.LRU, List.of(0, 64, 0, 64, 0, 64)));
        d2.setPergunta("Mesmo repetindo a sequência de acessos 3 vezes consecutivas, por que a taxa de acerto é 0%?");
        d2.setOpcoes(List.of(
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("A", "Porque ocorre conflito de linha (Thrashing): o bloco 4 expulsa o bloco 0 e vice-versa continuamente"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("B", "Porque o tamanho de bloco de 16 bytes é insuficiente para a arquitetura"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("C", "Porque a política LRU é incompatível com o mapeamento direto"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("D", "Porque a capacidade total da cache é excedida em todas as suas linhas")
        ));
        d2.setOpcaoCorretaId("A");
        d2.setDica("Calcule o índice de linha para o Bloco 0 (endereço 0) e para o Bloco 4 (endereço 64).");
        d2.setExplicacao("Perfeito! Em uma cache com 4 linhas, o Bloco 0 (0 / 16 = 0 % 4 = 0) e o Bloco 4 (64 / 16 = 4 % 4 = 0) são mapeados para a Linha 0. A cada acesso alternado, um bloco substitui o outro antes que possa ser reutilizado, gerando 100% de faltas por conflito (Thrashing).");
        desafioSimuladorRepository.save(d2);

        br.com.cachly.backend.simulador.desafio.DesafioSimulador d3 = new br.com.cachly.backend.simulador.desafio.DesafioSimulador();
        d3.setId("solucao-associativa");
        d3.setTitulo("Eliminando o Conflito com Associatividade (2-Way)");
        d3.setDescricao("Para solucionar o problema de conflito visto no desafio anterior sem o alto custo de hardware de um totalmente associativo, organizamos as linhas em conjuntos com 2 vias (2-Way Set Associative).");
        d3.setDificuldade(DificuldadeQuestao.MEDIO);
        d3.setXpRecompensa(60);
        d3.setConfiguracao(new br.com.cachly.backend.simulador.SimulacaoRequest(64, 16, 2, br.com.cachly.backend.simulador.TipoMapeamento.CONJUNTO_ASSOCIATIVO, br.com.cachly.backend.simulador.PoliticaSubstituicao.LRU, List.of(0, 64, 0, 64, 0, 64)));
        d3.setPergunta("Executando a mesma sequência [0, 64, 0, 64, 0, 64], quantos HITS são obtidos agora na cache associativa por conjunto?");
        d3.setOpcoes(List.of(
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("A", "4 Hits (os dois primeiros acessos são misses de aquecimento e os quatro seguintes são hits)"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("B", "6 Hits (todas as requisições encontram os blocos em cache)"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("C", "0 Hits (o conflito persiste pois ambos ainda mapeiam para o conjunto 0)"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("D", "2 Hits (apenas o primeiro bloco consegue ser preservado)")
        ));
        d3.setOpcaoCorretaId("A");
        d3.setDica("Agora o Conjunto 0 possui 2 linhas (vias). O Bloco 0 e o Bloco 4 cabem simultaneamente no mesmo conjunto?");
        d3.setExplicacao("Excelente dedução! Com 2 vias no Conjunto 0, o Bloco 0 reside na Via 0 e o Bloco 4 ocupa a Via 1. Ambos permanecem na cache juntos, eliminando os misses por conflito e convertendo todas as 4 repetições seguintes em HITS.");
        desafioSimuladorRepository.save(d3);

        br.com.cachly.backend.simulador.desafio.DesafioSimulador d4 = new br.com.cachly.backend.simulador.desafio.DesafioSimulador();
        d4.setId("lru-vs-fifo");
        d4.setTitulo("Duelo de Políticas: Localidade Temporal com LRU");
        d4.setDescricao("Quando uma cache totalmente associativa fica cheia, a política de substituição precisa escolher qual bloco descartar. O algoritmo LRU (Least Recently Used) descarta o bloco que está há mais tempo sem sofrer acesso.");
        d4.setDificuldade(DificuldadeQuestao.DIFICIL);
        d4.setXpRecompensa(80);
        d4.setConfiguracao(new br.com.cachly.backend.simulador.SimulacaoRequest(64, 16, null, br.com.cachly.backend.simulador.TipoMapeamento.TOTALMENTE_ASSOCIATIVO, br.com.cachly.backend.simulador.PoliticaSubstituicao.LRU, List.of(0, 16, 32, 48, 0, 80)));
        d4.setPergunta("Ao solicitar o endereço 80 (bloco 5) no último passo, qual bloco é substituído da cache pela política LRU?");
        d4.setOpcoes(List.of(
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("A", "Bloco 1 (endereço 16), pois o Bloco 0 foi reutilizado no passo 5 e teve sua prioridade renovada"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("B", "Bloco 0 (endereço 0), pois foi o primeiro a ser inserido na cache"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("C", "Bloco 3 (endereço 48), por se encontrar na última linha física"),
                new br.com.cachly.backend.simulador.desafio.OpcaoDesafioEmbeddable("D", "Nenhum bloco é substituído porque a memória cache expande dinamicamente")
        ));
        d4.setOpcaoCorretaId("A");
        d4.setDica("Observe atentamente o passo 5: o Bloco 0 foi acessado novamente logo antes do Bloco 5 chegar!");
        d4.setExplicacao("Brilhante! O Bloco 0 entrou primeiro, mas seu acesso no passo 5 atualizou sua data de utilização para o tempo mais recente. Quando o Bloco 5 requisitou espaço no passo 6, o bloco com menor utilização recente era o Bloco 1 (acessado no passo 2), sendo ele a vítima correta do LRU.");
        desafioSimuladorRepository.save(d4);
    }
}
