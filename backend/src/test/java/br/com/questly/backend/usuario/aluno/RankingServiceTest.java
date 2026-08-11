package br.com.questly.backend.usuario.aluno;

import br.com.questly.backend.usuario.PerfilUsuario;
import br.com.questly.backend.usuario.Usuario;
import br.com.questly.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RankingService rankingService;

    private Usuario usuario1;
    private Usuario usuario2;

    @BeforeEach
    void setUp() {
        usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setNome("Aluno Top 1");
        usuario1.setXpTotal(500);
        usuario1.setNivel(10);
        
        usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setNome("Aluno Top 2");
        usuario2.setXpTotal(300);
        usuario2.setNivel(6);
    }

    @Test
    void listarRanking_DeveRetornarRankingComPosicao() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Usuario> paginaUsuarios = new PageImpl<>(List.of(usuario1, usuario2));
        
        when(usuarioRepository.findByPerfil(eq(PerfilUsuario.ALUNO), any(Pageable.class)))
                .thenReturn(paginaUsuarios);

        // Act
        Page<RankingResponse> result = rankingService.listarRanking(pageRequest);

        // Assert
        assertEquals(2, result.getContent().size());
        
        RankingResponse top1 = result.getContent().get(0);
        assertEquals(1, top1.posicao());
        assertEquals("Aluno Top 1", top1.nome());
        assertEquals(500, top1.xpTotal());
        assertEquals(10, top1.nivel());
        
        RankingResponse top2 = result.getContent().get(1);
        assertEquals(2, top2.posicao());
        assertEquals("Aluno Top 2", top2.nome());
    }

    @Test
    void listarRanking_DeveCalcularPosicaoCorretamenteParaPagina2() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(1, 10); // Página 2 (index 1), tamanho 10
        Page<Usuario> paginaUsuarios = new PageImpl<>(List.of(usuario2));
        
        when(usuarioRepository.findByPerfil(eq(PerfilUsuario.ALUNO), any(Pageable.class)))
                .thenReturn(paginaUsuarios);

        // Act
        Page<RankingResponse> result = rankingService.listarRanking(pageRequest);

        // Assert
        assertEquals(1, result.getContent().size());
        
        RankingResponse top1 = result.getContent().get(0);
        assertEquals(11, top1.posicao()); // 1 (page) * 10 (size) + 0 (index) + 1
    }
}
