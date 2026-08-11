package br.com.cachly.backend.resposta;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.alternativa.AlternativaRepository;
import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespostaServiceTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private AlternativaRepository alternativaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TentativaQuestaoRepository tentativaQuestaoRepository;

    @Mock
    private XpService xpService;

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
    }

    @Test
    void deveRegistrarRespostaCorretaEConcederXp() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(questaoRepository.findByIdAndAtivaTrue(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findByIdAndQuestaoIdAndAtivaTrue(100L, 1L)).thenReturn(Optional.of(alternativaCorreta));
        when(xpService.calcularXpGanho(questao)).thenReturn(10);
        when(xpService.calcularNivel(10)).thenReturn(1);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        TentativaQuestao tentativaSalva = new TentativaQuestao();
        tentativaSalva.setId(500L);
        when(tentativaQuestaoRepository.save(any(TentativaQuestao.class))).thenReturn(tentativaSalva);

        RespostaRequest request = new RespostaRequest(100L);
        RespostaResponse response = respostaService.responder(1L, request, usuario);

        assertNotNull(response);
        assertEquals(500L, response.tentativaId());
        assertTrue(response.correta());
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

        verify(usuarioRepository).save(usuario);
        assertEquals(10, usuario.getXpTotal());
        assertEquals(1, usuario.getNivel());
    }

    @Test
    void deveRegistrarRespostaIncorretaSemConcederXp() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(questaoRepository.findByIdAndAtivaTrue(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findByIdAndQuestaoIdAndAtivaTrue(101L, 1L)).thenReturn(Optional.of(alternativaIncorreta));

        TentativaQuestao tentativaSalva = new TentativaQuestao();
        tentativaSalva.setId(501L);
        when(tentativaQuestaoRepository.save(any(TentativaQuestao.class))).thenReturn(tentativaSalva);

        RespostaRequest request = new RespostaRequest(101L);
        RespostaResponse response = respostaService.responder(1L, request, usuario);

        assertNotNull(response);
        assertEquals(501L, response.tentativaId());
        assertFalse(response.correta());
        assertEquals("Bit é a menor unidade de informação em computação.", response.explicacao());
        assertEquals(0, response.xpConcedido());
        assertEquals(1, response.nivelAtual());
        assertEquals(0, response.xpTotal());

        verify(xpService, never()).calcularXpGanho(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveSubirDeNivelQuandoXpAtingirOLimiar() {
        usuario.setXpTotal(90);
        usuario.setNivel(1);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(questaoRepository.findByIdAndAtivaTrue(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findByIdAndQuestaoIdAndAtivaTrue(100L, 1L)).thenReturn(Optional.of(alternativaCorreta));
        when(xpService.calcularXpGanho(questao)).thenReturn(10);
        when(xpService.calcularNivel(100)).thenReturn(2);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        TentativaQuestao tentativaSalva = new TentativaQuestao();
        tentativaSalva.setId(502L);
        when(tentativaQuestaoRepository.save(any(TentativaQuestao.class))).thenReturn(tentativaSalva);

        RespostaResponse response = respostaService.responder(1L, new RespostaRequest(100L), usuario);

        assertEquals(2, response.nivelAtual());
        assertEquals(100, response.xpTotal());
        assertEquals(10, response.xpConcedido());
    }

    @Test
    void deveLancarExcecaoQuandoQuestaoInexistenteOuInativa() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(questaoRepository.findByIdAndAtivaTrue(99L)).thenReturn(Optional.empty());

        RespostaRequest request = new RespostaRequest(100L);

        assertThrows(RecursoNaoEncontradoException.class, () -> respostaService.responder(99L, request, usuario));
        verify(tentativaQuestaoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoAlternativaInexistenteInativaOuNaoPertencenteAQuestao() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(questaoRepository.findByIdAndAtivaTrue(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findByIdAndQuestaoIdAndAtivaTrue(999L, 1L)).thenReturn(Optional.empty());

        RespostaRequest request = new RespostaRequest(999L);

        assertThrows(RecursoNaoEncontradoException.class, () -> respostaService.responder(1L, request, usuario));
        verify(tentativaQuestaoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioInativo() {
        usuario.setAtivo(false);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        RespostaRequest request = new RespostaRequest(100L);

        assertThrows(ConflitoDeDadosException.class, () -> respostaService.responder(1L, request, usuario));
        verify(tentativaQuestaoRepository, never()).save(any());
    }
}
