package br.com.cachly.backend.resposta;

import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.simulador.desafio.DesafioRespondidoEvent;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GamificacaoEventListenerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private XpService xpService = new XpService();

    @InjectMocks
    private GamificacaoEventListener listener;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listener, "timezone", "America/Sao_Paulo");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setXpTotal(100);
        usuario.setXpSemanal(20);
        usuario.setNivel(2);
        usuario.setDiasOfensiva(0);
        usuario.setDataUltimaOfensiva(LocalDate.now().minusDays(2)); // Perdeu a ofensiva
    }

    @Test
    void deveAtualizarOfensivaEXpAoResponderQuestao() {
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));

        Questao questao = new Questao();
        questao.setXpBase(10);
        questao.setDificuldade(DificuldadeQuestao.FACIL);

        QuestaoRespondidaEvent event = new QuestaoRespondidaEvent(usuario, questao, true, true);
        listener.onQuestaoRespondida(event);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario salvo = captor.getValue();
        assertEquals(110, salvo.getXpTotal());
        assertEquals(30, salvo.getXpSemanal());
        assertEquals(1, salvo.getDiasOfensiva()); // Ofensiva iniciada hoje
        assertEquals(LocalDate.now(), salvo.getDataUltimaOfensiva());
    }

    @Test
    void deveAtualizarOfensivaEXpAoResponderDesafio() {
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(usuario));

        DesafioRespondidoEvent event = new DesafioRespondidoEvent(usuario, "desafio-1", 50, true, true);
        listener.onDesafioRespondido(event);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario salvo = captor.getValue();
        assertEquals(150, salvo.getXpTotal()); // 100 + 50
        assertEquals(70, salvo.getXpSemanal()); // 20 + 50
        assertEquals(1, salvo.getDiasOfensiva()); // Ofensiva iniciada hoje
        assertEquals(LocalDate.now(), salvo.getDataUltimaOfensiva());
    }
}
