package br.com.cachly.backend.comum.config;

import br.com.cachly.backend.categoria.Categoria;
import br.com.cachly.backend.categoria.CategoriaRepository;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import br.com.cachly.backend.resposta.DesempenhoCategoriaProjection;
import br.com.cachly.backend.resposta.TentativaQuestaoRepository;
import br.com.cachly.backend.simulador.desafio.DesafioConcluidoRepository;
import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import br.com.cachly.backend.usuario.aluno.RankingSemanalHistoricoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import br.com.cachly.backend.resposta.XpService;
import jakarta.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DataSeederTest {

    @TestConfiguration
    static class Config {
        @Bean
        public DataSeeder dataSeeder(
                UsuarioRepository usuarioRepository,
                CategoriaRepository categoriaRepository,
                QuestaoRepository questaoRepository,
                TentativaQuestaoRepository tentativaQuestaoRepository,
                RankingSemanalHistoricoRepository rankingSemanalHistoricoRepository,
                DesafioConcluidoRepository desafioConcluidoRepository,
                PasswordEncoder passwordEncoder,
                XpService xpService,
                EntityManager entityManager
        ) {
            return new DataSeeder(
                    usuarioRepository,
                    categoriaRepository,
                    questaoRepository,
                    tentativaQuestaoRepository,
                    rankingSemanalHistoricoRepository,
                    desafioConcluidoRepository,
                    passwordEncoder,
                    xpService,
                    entityManager
            );
        }
    }

    @Autowired
    private DataSeeder dataSeeder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private TentativaQuestaoRepository tentativaQuestaoRepository;

    @Autowired
    private DesafioConcluidoRepository desafioConcluidoRepository;

    @Autowired
    private RankingSemanalHistoricoRepository rankingSemanalHistoricoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Deve popular cenário acadêmico completo com consistência matemática e idempotência")
    void devePopularCenarioAcademicoCompleto() throws Exception {
        // 1. Executa o Seeder
        dataSeeder.run();

        // 2. Validação dos Usuários
        Usuario admin = usuarioRepository.findByEmailIgnoreCase("admin@cachly.com")
                .orElseThrow(() -> new AssertionError("Admin não encontrado"));
        assertEquals(PerfilUsuario.ADMINISTRADOR, admin.getPerfil());
        assertTrue(admin.getAtivo());
        assertTrue(passwordEncoder.matches("admin123", admin.getSenhaHash()));

        Usuario aluno = usuarioRepository.findByEmailIgnoreCase("aluno@teste.com")
                .orElseThrow(() -> new AssertionError("Aluno de teste não encontrado"));
        assertEquals(PerfilUsuario.ALUNO, aluno.getPerfil());
        assertEquals(3, aluno.getNivel());
        assertEquals(285, aluno.getXpTotal());
        assertEquals(180, aluno.getXpSemanal());
        assertEquals(5, aluno.getDiasOfensiva());
        assertTrue(passwordEncoder.matches("senha123", aluno.getSenhaHash()));

        Usuario beatriz = usuarioRepository.findByEmailIgnoreCase("beatriz@teste.com")
                .orElseThrow(() -> new AssertionError("Beatriz não encontrada"));
        assertEquals(4, beatriz.getNivel());
        assertEquals(520, beatriz.getXpTotal());
        assertEquals(260, beatriz.getXpSemanal());

        Usuario carlos = usuarioRepository.findByEmailIgnoreCase("carlos@teste.com")
                .orElseThrow(() -> new AssertionError("Carlos não encontrado"));
        assertEquals(3, carlos.getNivel());
        assertEquals(340, carlos.getXpTotal());
        assertEquals(140, carlos.getXpSemanal());

        Usuario daniela = usuarioRepository.findByEmailIgnoreCase("daniela@teste.com")
                .orElseThrow(() -> new AssertionError("Daniela não encontrada"));
        assertEquals(2, daniela.getNivel());
        assertEquals(160, daniela.getXpTotal());
        assertEquals(70, daniela.getXpSemanal());

        // 3. Validação das Categorias e Questões
        List<Categoria> categorias = categoriaRepository.findAll();
        assertEquals(4, categorias.size(), "Devem existir 4 categorias pedagógicas");

        List<Questao> todasQuestoes = questaoRepository.findAll();
        assertEquals(12, todasQuestoes.size(), "Devem existir 12 questões no total");

        for (Categoria cat : categorias) {
            List<Questao> questoesDaCategoria = todasQuestoes.stream()
                    .filter(q -> q.getCategoria().getId().equals(cat.getId()))
                    .toList();
            assertEquals(3, questoesDaCategoria.size(), "Cada categoria deve ter exatamente 3 questões");

            boolean temFacil = questoesDaCategoria.stream().anyMatch(q -> q.getDificuldade() == DificuldadeQuestao.FACIL);
            boolean temMedio = questoesDaCategoria.stream().anyMatch(q -> q.getDificuldade() == DificuldadeQuestao.MEDIO);
            boolean temDificil = questoesDaCategoria.stream().anyMatch(q -> q.getDificuldade() == DificuldadeQuestao.DIFICIL);

            assertTrue(temFacil, "Categoria " + cat.getNome() + " deve conter questão FÁCIL");
            assertTrue(temMedio, "Categoria " + cat.getNome() + " deve conter questão MÉDIA");
            assertTrue(temDificil, "Categoria " + cat.getNome() + " deve conter questão DIFÍCIL");

            for (Questao q : questoesDaCategoria) {
                assertFalse(q.getExplicacao().isBlank(), "Explicação não pode estar vazia");
                assertTrue(q.getAlternativas().size() >= 2 && q.getAlternativas().size() <= 5,
                        "Questão deve ter entre 2 e 5 alternativas");
                long corretas = q.getAlternativas().stream().filter(a -> a.getCorreta()).count();
                assertEquals(1, corretas, "Questão deve ter exatamente 1 alternativa correta");
            }
        }

        // 4. Validação do Histórico de Tentativas e Estatísticas de Desempenho
        long totalTentativas = tentativaQuestaoRepository.countByUsuarioId(aluno.getId());
        assertEquals(20, totalTentativas, "Aluno demo deve ter 20 tentativas registradas");

        List<DesempenhoCategoriaProjection> stats = tentativaQuestaoRepository.findEstatisticasPorCategoria(aluno.getId());
        assertEquals(4, stats.size(), "Devem existir estatísticas para as 4 categorias");

        DesempenhoCategoriaProjection piorCategoria = stats.stream()
                .reduce((pior, atual) -> {
                    double taxaAtual = (double) atual.getAcertos() / atual.getTotalTentativas();
                    double taxaPior = (double) pior.getAcertos() / pior.getTotalTentativas();
                    return taxaAtual < taxaPior ? atual : pior;
                })
                .orElseThrow();

        assertEquals("Políticas de Substituição", piorCategoria.getCategoriaNome(),
                "A categoria mais fraca deve ser Políticas de Substituição para o algoritmo de recomendação");
        assertEquals(1L, piorCategoria.getAcertos());
        assertEquals(5L, piorCategoria.getTotalTentativas());

        // 5. Validação dos Desafios Concluídos
        assertTrue(desafioConcluidoRepository.existsByUsuarioIdAndDesafioId(aluno.getId(), "cold-miss"));
        assertTrue(desafioConcluidoRepository.existsByUsuarioIdAndDesafioId(aluno.getId(), "thrashing-direto"));
        assertFalse(desafioConcluidoRepository.existsByUsuarioIdAndDesafioId(aluno.getId(), "solucao-associativa"));
        assertFalse(desafioConcluidoRepository.existsByUsuarioIdAndDesafioId(aluno.getId(), "lru-vs-fifo"));

        // 6. Validação do Histórico Semanal de Ranking
        List<java.time.LocalDate> datasHistorico = rankingSemanalHistoricoRepository.findDatasDisponiveis();
        assertEquals(1, datasHistorico.size(), "Deve existir 1 snapshot de ranking semanal anterior");

        var paginaHistorico = rankingSemanalHistoricoRepository.findByDataSemana(datasHistorico.get(0), org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(4, paginaHistorico.getTotalElements(), "Snapshot anterior deve conter os 4 alunos");

        // 7. Teste de Idempotência: rodar novamente não deve quebrar nem duplicar dados
        assertDoesNotThrow(() -> dataSeeder.run());
    }
}
