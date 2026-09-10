package br.com.cachly.backend.simulador;

import br.com.cachly.backend.simulador.desafio.DesafioCacheService;
import br.com.cachly.backend.simulador.desafio.DesafioConcluidoRepository;
import br.com.cachly.backend.simulador.desafio.ResultadoDesafioResponse;
import br.com.cachly.backend.simulador.desafio.VerificarDesafioRequest;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DesafioConcorrenciaIntegracaoTest {

    @Autowired
    private DesafioCacheService desafioCacheService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DesafioConcluidoRepository desafioConcluidoRepository;

    private Usuario aluno;

    @BeforeEach
    void setUp() {
        aluno = new Usuario();
        aluno.setNome("Aluno Desafio Concorrente");
        aluno.setEmail("aluno.desafio.concorrente." + UUID.randomUUID() + "@email.com");
        aluno.setSenhaHash("senha123");
        aluno.setPerfil(PerfilUsuario.ALUNO);
        aluno.setAtivo(true);
        aluno.setXpTotal(0);
        aluno.setXpSemanal(0);
        aluno.setNivel(1);
        aluno.setDiasOfensiva(0);
        aluno = usuarioRepository.saveAndFlush(aluno);
    }

    @AfterEach
    void tearDown() {
        desafioConcluidoRepository.deleteAll();
        usuarioRepository.deleteById(aluno.getId());
    }

    @Test
    @DisplayName("Deve responder graciosamente sem erro 500 nem rollback em verificações simultâneas de desafio")
    void deveResponderGraciosamenteEmConcorrenciaDeDesafio() throws Exception {
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threads);

        List<Future<ResultadoDesafioResponse>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                VerificarDesafioRequest request = new VerificarDesafioRequest("A");
                try {
                    return desafioCacheService.verificarDesafio("cold-miss", request, aluno);
                } finally {
                    finishLatch.countDown();
                }
            }));
        }

        startLatch.countDown();
        finishLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        List<ResultadoDesafioResponse> responses = new ArrayList<>();
        List<Throwable> exceptions = new ArrayList<>();

        for (Future<ResultadoDesafioResponse> f : futures) {
            try {
                responses.add(f.get());
            } catch (ExecutionException ee) {
                exceptions.add(ee.getCause());
            }
        }

        System.out.println("=== RESULTADO DO TESTE DE CONCORRÊNCIA DESAFIO (PÓS-FIX) ===");
        System.out.println("Respostas obtidas: " + responses.size());
        System.out.println("Exceções capturadas: " + exceptions.size());
        for (int i = 0; i < responses.size(); i++) {
            System.out.println("Thread " + i + " - correto: " + responses.get(i).correto() +
                    ", xpGanho: " + responses.get(i).xpGanho() +
                    ", xpTotalAtual: " + responses.get(i).xpTotalAtual());
        }

        Usuario usuarioAtualizado = usuarioRepository.findById(aluno.getId()).orElseThrow();
        System.out.println("XP Total final do usuário no banco: " + usuarioAtualizado.getXpTotal());
        System.out.println("=============================================================");

        // Nenhuma exceção (zero UnexpectedRollbackException ou 500)
        assertTrue(exceptions.isEmpty(), "Nenhuma requisição deve falhar com exceção / rollback-only");
        assertEquals(2, responses.size(), "Ambas as threads devem receber resposta com sucesso HTTP 200");

        // Apenas UMA thread deve conceder XP
        long threadsComXp = responses.stream().filter(r -> r.xpGanho() > 0).count();
        assertEquals(1, threadsComXp, "Apenas uma das chamadas simultâneas deve conceder XP");
        assertEquals(30, usuarioAtualizado.getXpTotal(), "O XP total no banco deve ser 30 (concedido uma única vez)");
    }
}
