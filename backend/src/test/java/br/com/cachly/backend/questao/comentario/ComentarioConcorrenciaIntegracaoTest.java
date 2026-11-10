package br.com.cachly.backend.questao.comentario;

import br.com.cachly.backend.categoria.*;
import br.com.cachly.backend.questao.*;
import br.com.cachly.backend.usuario.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ComentarioConcorrenciaIntegracaoTest {
    @Autowired private ComentarioService service;
    @Autowired private ComentarioRepository comentarios;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private CategoriaRepository categorias;
    @Autowired private QuestaoRepository questoes;
    @Autowired private TransactionTemplate transacao;
    @MockitoBean private UsuarioLogadoService usuarioLogado;

    @Test
    void deveContabilizarVotosSimultaneosDeUsuariosDiferentes() throws Exception {
        var identificador = UUID.randomUUID().toString();
        var primeiro = criarUsuario("primeiro-" + identificador);
        var segundo = criarUsuario("segundo-" + identificador);
        var categoria = new Categoria();
        categoria.setNome("Categoria " + identificador);
        categoria = categorias.saveAndFlush(categoria);
        var questao = new Questao();
        questao.setCategoria(categoria);
        questao.setEnunciado("Questão de concorrência");
        questao.setExplicacao("Explicação");
        questao.setDificuldade(DificuldadeQuestao.FACIL);
        questao.setXpBase(10);
        questao = questoes.saveAndFlush(questao);
        var comentario = new Comentario();
        comentario.setUsuario(primeiro);
        comentario.setQuestao(questao);
        comentario.setConteudo("Comentário de teste");
        Long comentarioId = comentarios.saveAndFlush(comentario).getId();
        var usuarioDaThread = new ThreadLocal<Usuario>();
        when(usuarioLogado.obterUsuarioAtual()).thenAnswer(invocacao -> usuarioDaThread.get());
        try (var executor = Executors.newFixedThreadPool(2)) {
            var inicio = new CountDownLatch(1);
            var a = executor.submit(() -> votar(primeiro, usuarioDaThread, inicio, comentarioId));
            var b = executor.submit(() -> votar(segundo, usuarioDaThread, inicio, comentarioId));
            inicio.countDown();
            a.get(10, TimeUnit.SECONDS);
            b.get(10, TimeUnit.SECONDS);
            transacao.executeWithoutResult(status -> {
                var atual = comentarios.findById(comentarioId).orElseThrow();
                assertEquals(2, atual.getUpvotes());
                assertEquals(2, atual.getUpvotedBy().size());
            });
        } finally {
            comentarios.deleteById(comentarioId);
            questoes.deleteById(questao.getId());
            categorias.deleteById(categoria.getId());
            usuarios.deleteById(primeiro.getId());
            usuarios.deleteById(segundo.getId());
        }
    }

    private void votar(Usuario usuario, ThreadLocal<Usuario> atual, CountDownLatch inicio, Long comentarioId) {
        atual.set(usuario);
        try {
            if (!inicio.await(5, TimeUnit.SECONDS)) throw new AssertionError("Tempo limite aguardando votos");
            service.darUpvote(comentarioId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        } finally {
            atual.remove();
        }
    }

    private Usuario criarUsuario(String nome) {
        var usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(nome + "@teste.com");
        usuario.setSenhaHash("hash-apenas-para-teste");
        return usuarios.saveAndFlush(usuario);
    }
}
