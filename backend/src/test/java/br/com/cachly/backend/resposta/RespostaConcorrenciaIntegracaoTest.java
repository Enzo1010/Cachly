package br.com.cachly.backend.resposta;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.alternativa.AlternativaRepository;
import br.com.cachly.backend.categoria.Categoria;
import br.com.cachly.backend.categoria.CategoriaRepository;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RespostaConcorrenciaIntegracaoTest {

    @Autowired
    private RespostaService respostaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private TentativaQuestaoRepository tentativaQuestaoRepository;

    private Usuario aluno;
    private Questao questao;
    private Alternativa alternativaCorreta;

    @BeforeEach
    void setUp() {
        aluno = new Usuario();
        aluno.setNome("Aluno Concorrente");
        aluno.setEmail("aluno.concorrente." + UUID.randomUUID() + "@email.com");
        aluno.setSenhaHash("senha123");
        aluno.setPerfil(PerfilUsuario.ALUNO);
        aluno.setAtivo(true);
        aluno.setXpTotal(0);
        aluno.setXpSemanal(0);
        aluno.setNivel(1);
        aluno.setDiasOfensiva(0);
        aluno = usuarioRepository.saveAndFlush(aluno);

        Categoria categoria = new Categoria();
        categoria.setNome("Categoria Concorrência " + UUID.randomUUID());
        categoria.setDescricao("Descrição");
        categoria.setAtiva(true);
        categoria = categoriaRepository.saveAndFlush(categoria);

        questao = new Questao();
        questao.setEnunciado("Enunciado da questão de teste de concorrência?");
        questao.setExplicacao("Explicação detalhada.");
        questao.setDificuldade(DificuldadeQuestao.FACIL);
        questao.setXpBase(10);
        questao.setAtiva(true);
        questao.setCategoria(categoria);
        questao = questaoRepository.saveAndFlush(questao);

        alternativaCorreta = new Alternativa();
        alternativaCorreta.setTexto("Alternativa Correta");
        alternativaCorreta.setCorreta(true);
        alternativaCorreta.setOrdem((short) 1);
        alternativaCorreta.setAtiva(true);
        alternativaCorreta.setQuestao(questao);
        alternativaCorreta = alternativaRepository.saveAndFlush(alternativaCorreta);

        Alternativa alternativaIncorreta = new Alternativa();
        alternativaIncorreta.setTexto("Alternativa Incorreta");
        alternativaIncorreta.setCorreta(false);
        alternativaIncorreta.setOrdem((short) 2);
        alternativaIncorreta.setAtiva(true);
        alternativaIncorreta.setQuestao(questao);
        alternativaRepository.saveAndFlush(alternativaIncorreta);

        questao.getAlternativas().addAll(List.of(alternativaCorreta, alternativaIncorreta));
    }

    @AfterEach
    void tearDown() {
        tentativaQuestaoRepository.deleteAll();
        alternativaRepository.deleteAll();
        questaoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve conceder XP exatamente uma vez quando duas requisições simultâneas respondem corretamente")
    void deveConcederXpApenasUmaVezEmRespostasConcorrentes() throws Exception {
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threads);

        List<Future<RespostaResponse>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await(); // Aguarda todas as threads estarem prontas
                RespostaRequest request = new RespostaRequest(alternativaCorreta.getId());
                try {
                    return respostaService.responder(questao.getId(), request, aluno);
                } finally {
                    finishLatch.countDown();
                }
            }));
        }

        // Libera as threads simultaneamente
        startLatch.countDown();
        finishLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        List<RespostaResponse> responses = new ArrayList<>();
        for (Future<RespostaResponse> f : futures) {
            responses.add(f.get());
        }

        long countComXp = responses.stream().filter(r -> r.xpConcedido() > 0).count();
        Usuario usuarioAtualizado = usuarioRepository.findById(aluno.getId()).orElseThrow();

        System.out.println("=== RESULTADO DO TESTE DE CONCORRÊNCIA ===");
        System.out.println("Respostas com XP concedido > 0: " + countComXp);
        for (int i = 0; i < responses.size(); i++) {
            System.out.println("Thread " + i + " - xpConcedido: " + responses.get(i).xpConcedido() +
                    ", xpTotal: " + responses.get(i).xpTotal() +
                    ", nivelAtual: " + responses.get(i).nivelAtual());
        }
        System.out.println("XP Total final do usuário no banco: " + usuarioAtualizado.getXpTotal());
        System.out.println("==========================================");

        // Cada questão fácil vale 10 XP. Apenas UMA das threads pode conceder XP!
        assertEquals(1, countComXp, "Apenas uma resposta concorrente deveria conceder XP");
        assertEquals(10, usuarioAtualizado.getXpTotal(), "O XP total no banco deveria ser 10 (concedido uma única vez)");
    }
}
