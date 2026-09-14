package br.com.cachly.backend.comum.config;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.categoria.Categoria;
import br.com.cachly.backend.categoria.CategoriaRepository;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
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
import jakarta.persistence.EntityManager;
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
 * O comportamento intencional é limpar e repopular o banco a cada inicialização para garantir
 * um ambiente previsível e completo para bancas e apresentações.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final QuestaoRepository questaoRepository;
    private final TentativaQuestaoRepository tentativaQuestaoRepository;
    private final RankingSemanalHistoricoRepository rankingSemanalHistoricoRepository;
    private final DesafioConcluidoRepository desafioConcluidoRepository;
    private final PasswordEncoder passwordEncoder;
    private final XpService xpService;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Limpar dados anteriores para manter o cenário limpo e previsível a cada inicialização
        limparDadosAntigos();

        if (usuarioRepository.findByEmailIgnoreCase("aluno@teste.com").isPresent()) {
            return; // Idempotência defensiva
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
        // 4. DESAFIOS DO SIMULADOR (DESAFIOS CONCLUÍDOS)
        // Conclui 2 desafios (80 XP total) e deixa 2 em aberto para demo ao vivo
        // =========================================================================
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

    private void limparDadosAntigos() {
        entityManager.createNativeQuery("DELETE FROM desafios_concluidos").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM ranking_semanal_historico").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tentativas_questao").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM alternativas").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM questoes").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM categorias").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM usuarios WHERE email IN ('admin@cachly.com', 'aluno@teste.com', 'beatriz@teste.com', 'carlos@teste.com', 'daniela@teste.com')").executeUpdate();
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
                                 List<String> alternativasErradasTxt) {
        Questao q = new Questao();
        q.setCategoria(categoria);
        q.setEnunciado(enunciado);
        q.setDificuldade(dificuldade);
        q.setXpBase(xpBase);
        q.setExplicacao(explicacao);
        q.setAtiva(true);

        short ordem = 1;
        Alternativa correta = new Alternativa();
        correta.setTexto(alternativaCorretaTxt);
        correta.setCorreta(true);
        correta.setOrdem(ordem++);
        correta.setQuestao(q);
        correta.setAtiva(true);

        List<Alternativa> lista = new ArrayList<>();
        lista.add(correta);

        for (String erradaTxt : alternativasErradasTxt) {
            Alternativa errada = new Alternativa();
            errada.setTexto(erradaTxt);
            errada.setCorreta(false);
            errada.setOrdem(ordem++);
            errada.setQuestao(q);
            errada.setAtiva(true);
            lista.add(errada);
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
}
