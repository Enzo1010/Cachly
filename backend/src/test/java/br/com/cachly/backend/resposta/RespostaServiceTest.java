package br.com.cachly.backend.resposta;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespostaServiceTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private TentativaQuestaoRepository tentativaQuestaoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Spy
    private XpService xpService = new XpService();

    @Mock
    private br.com.cachly.backend.usuario.UsuarioRepository usuarioRepository;

    @InjectMocks
    private RespostaService respostaService;

    private Usuario usuario;
    private Questao questao;
    private Alternativa alternativaCorreta;
    private Alternativa alternativaIncorreta;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("Aluno Teste");
        usuario.setEmail("aluno@teste.com");
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setAtivo(true);
        usuario.setXpTotal(0);
        usuario.setNivel(1);

        questao = new Questao();
        questao.setId(1L);
        questao.setEnunciado("O que é um bit?");
        questao.setExplicacao("Bit é a menor unidade de informação em computação.");
        questao.setDificuldade(DificuldadeQuestao.FACIL);
        questao.setXpBase(10);
        questao.setAtiva(true);

        alternativaCorreta = new Alternativa();
        alternativaCorreta.setId(100L);
        alternativaCorreta.setQuestao(questao);
        alternativaCorreta.setTexto("Dígito binário 0 ou 1");
        alternativaCorreta.setCorreta(true);
        alternativaCorreta.setAtiva(true);

        alternativaIncorreta = new Alternativa();
        alternativaIncorreta.setId(101L);
        alternativaIncorreta.setQuestao(questao);
        alternativaIncorreta.setTexto("Byte de 8 bits");
        alternativaIncorreta.setCorreta(false);
        alternativaIncorreta.setAtiva(true);

        questao.getAlternativas().add(alternativaCorreta);
        questao.getAlternativas().add(alternativaIncorreta);
    }

    @Test
    void deveRegistrarRespostaCorretaEConcederXp() {
        when(questaoRepository.findByIdAndAtivaTrue(1L)).thenReturn(Optional.of(questao));
        when(tentativaQuestaoRepository.existsByUsuarioIdAndQuestaoIdAndCorretaTrue(10L, 1L)).thenReturn(false);
        when(xpService.calcularXpGanho(questao)).thenReturn(10);
        when(xpService.nomeDoNivel(1)).thenReturn("Estagiário");

        TentativaQuestao tentativaSalva = new TentativaQuestao();
        tentativaSalva.setId(500L);
        when(tentativaQuestaoRepository.save(any(TentativaQuestao.class))).thenReturn(tentativaSalva);

        Usuario usuarioPosEvento = new Usuario();
        usuarioPosEvento.setId(10L);
        usuarioPosEvento.setXpTotal(10);
        usuarioPosEvento.setNivel(1);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioPosEvento));

        RespostaRequest request = new RespostaRequest(100L);
        RespostaResponse response = respostaService.responder(1L, request, usuario);

        assertNotNull(response);
        assertEquals(500L, response.tentativaId());
        assertTrue(response.correta());
        assertEquals(100L, response.alternativaCorretaId());
        assertEquals("Bit é a menor unidade de informação em computação.", response.explicacao());
        assertEquals(10, response.xpConcedido());
        assertEquals(1, response.nivelAtual());
        assertEquals(10, response.xpTotal());

        ArgumentCaptor<TentativaQuestao> captor = ArgumentCaptor.forClass(TentativaQuestao.class);
        verify(tentativaQuestaoRepository).save(captor.capture());
        TentativaQuestao capturada = captor.getValue();
        assertEquals(usuario, capturada.getUsuario());
        assertEquals(questao, capturada.getQuestao());
        assertEquals(alternativaCorreta, capturada.getAlternativa());
        assertTrue(capturada.getCorreta());
        assertEquals(10, capturada.getXpConcedido());

        ArgumentCaptor<QuestaoRespondidaEvent> eventCaptor = ArgumentCaptor.forClass(QuestaoRespondidaEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertTrue(eventCaptor.getValue().correta());
        assertTrue(eventCaptor.getValue().primeiraVezCorreta());
    }

    @Test
    void deveRegistrarRespostaIncorretaSemConcederXp() {
        when(questaoRepository.findByIdAndAtivaTrue(1L)).thenReturn(Optional.of(questao));
        when(xpService.nomeDoNivel(1)).thenReturn("Estagiário");

        TentativaQuestao tentativaSalva = new TentativaQuestao();
        tentativaSalva.setId(501L);
        when(tentativaQuestaoRepository.save(any(TentativaQuestao.class))).thenReturn(tentativaSalva);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        RespostaRequest request = new RespostaRequest(101L);
        RespostaResponse response = respostaService.responder(1L, request, usuario);

        assertNotNull(response);
        assertEquals(501L, response.tentativaId());
        assertFalse(response.correta());
        assertEquals(100L, response.alternativaCorretaId());
        assertEquals("Bit é a menor unidade de informação em computação.", response.explicacao());
        assertEquals(0, response.xpConcedido());
        assertEquals(1, response.nivelAtual());
        assertEquals(0, response.xpTotal());

        verify(xpService, never()).calcularXpGanho(any());

        ArgumentCaptor<QuestaoRespondidaEvent> eventCaptor = ArgumentCaptor.forClass(QuestaoRespondidaEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertFalse(eventCaptor.getValue().correta());
        assertFalse(eventCaptor.getValue().primeiraVezCorreta());
    }

    @Test
    void deveLancarExcecaoQuandoQuestaoInexistenteOuInativa() {
        when(questaoRepository.findByIdAndAtivaTrue(99L)).thenReturn(Optional.empty());

        RespostaRequest request = new RespostaRequest(100L);

        assertThrows(RecursoNaoEncontradoException.class, () -> respostaService.responder(99L, request, usuario));
        verify(tentativaQuestaoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deveLancarExcecaoQuandoAlternativaInexistenteInativaOuNaoPertencenteAQuestao() {
        when(questaoRepository.findByIdAndAtivaTrue(1L)).thenReturn(Optional.of(questao));

        RespostaRequest request = new RespostaRequest(999L); // Alternativa não existe

        assertThrows(RecursoNaoEncontradoException.class, () -> respostaService.responder(1L, request, usuario));
        verify(tentativaQuestaoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioInativo() {
        usuario.setAtivo(false);

        RespostaRequest request = new RespostaRequest(100L);

        assertThrows(br.com.cachly.backend.comum.erro.RegraNegocioException.class, () -> respostaService.responder(1L, request, usuario));
        verify(tentativaQuestaoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}

