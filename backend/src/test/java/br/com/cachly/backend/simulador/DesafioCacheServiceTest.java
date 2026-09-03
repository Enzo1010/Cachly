package br.com.cachly.backend.simulador;

import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.resposta.XpService;
import br.com.cachly.backend.simulador.desafio.DesafioCacheResponse;
import br.com.cachly.backend.simulador.desafio.DesafioCacheService;
import br.com.cachly.backend.simulador.desafio.ResultadoDesafioResponse;
import br.com.cachly.backend.simulador.desafio.VerificarDesafioRequest;
import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesafioCacheServiceTest {

    @Spy
    private SimuladorCacheService simuladorCacheService = new SimuladorCacheService();

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private XpService xpService = new XpService();

    @InjectMocks
    private DesafioCacheService desafioCacheService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Aluno Teste");
        usuario.setEmail("aluno@teste.com");
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setXpTotal(100);
        usuario.setNivel(2);
    }

    @Test
    @DisplayName("Deve listar todos os desafios disponiveis")
    void deveListarDesafios() {
        List<DesafioCacheResponse> desafios = desafioCacheService.listarDesafios();

        assertNotNull(desafios);
        assertEquals(4, desafios.size());
        assertTrue(desafios.stream().anyMatch(d -> d.id().equals("cold-miss")));
        assertTrue(desafios.stream().anyMatch(d -> d.id().equals("thrashing-direto")));
        assertTrue(desafios.stream().anyMatch(d -> d.id().equals("solucao-associativa")));
        assertTrue(desafios.stream().anyMatch(d -> d.id().equals("lru-vs-fifo")));
    }

    @Test
    @DisplayName("Deve obter desafio por ID com sucesso")
    void deveObterDesafioPorId() {
        DesafioCacheResponse desafio = desafioCacheService.obterDesafio("thrashing-direto");

        assertNotNull(desafio);
        assertEquals("thrashing-direto", desafio.id());
        assertEquals(50, desafio.xpRecompensa());
        assertNotNull(desafio.configuracao());
        assertFalse(desafio.opcoes().isEmpty());
    }

    @Test
    @DisplayName("Deve lancar excecao ao buscar desafio inexistente")
    void deveLancarExcecaoParaDesafioInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () ->
                desafioCacheService.obterDesafio("desafio-que-nao-existe")
        );
    }

    @Test
    @DisplayName("Deve verificar resposta correta e conceder XP ao aluno")
    void deveVerificarRespostaCorretaEConcederXp() {
        VerificarDesafioRequest request = new VerificarDesafioRequest("A");

        ResultadoDesafioResponse resultado = desafioCacheService.verificarDesafio("cold-miss", request, usuario);

        assertTrue(resultado.correto());
        assertEquals("A", resultado.opcaoCorretaId());
        assertEquals(30, resultado.xpGanho());
        assertNotNull(resultado.simulacao());
        assertEquals(4, resultado.simulacao().totalMisses());
        assertEquals(0, resultado.simulacao().totalHits());

        // Verifica que o usuario acumulou XP
        assertEquals(130, usuario.getXpTotal());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve verificar resposta incorreta sem conceder XP")
    void deveVerificarRespostaIncorretaSemConcederXp() {
        VerificarDesafioRequest request = new VerificarDesafioRequest("B");

        ResultadoDesafioResponse resultado = desafioCacheService.verificarDesafio("cold-miss", request, usuario);

        assertFalse(resultado.correto());
        assertEquals("A", resultado.opcaoCorretaId());
        assertEquals(0, resultado.xpGanho());
        assertTrue(resultado.explicacao().contains("Dica:"));
        assertNotNull(resultado.simulacao());

        // Usuario nao deve ter ganho XP nem sido salvo
        assertEquals(100, usuario.getXpTotal());
        verify(usuarioRepository, never()).save(any());
    }
}
