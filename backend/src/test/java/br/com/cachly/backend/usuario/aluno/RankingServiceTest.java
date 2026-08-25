package br.com.cachly.backend.usuario.aluno;

import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
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

    private RankingProjection usuario1;
    private RankingProjection usuario2;

    @BeforeEach
    void setUp() {
        usuario1 = new RankingProjection() {
            public String getNome() { return "Aluno Top 1"; }
            public Integer getNivel() { return 10; }
            public Integer getXpSemanal() { return 500; }
            public Integer getDiasOfensiva() { return 5; }
        };
        
        usuario2 = new RankingProjection() {
            public String getNome() { return "Aluno Top 2"; }
            public Integer getNivel() { return 6; }
            public Integer getXpSemanal() { return 300; }
            public Integer getDiasOfensiva() { return 3; }
        };
    }

    @Test
    void listarRanking_DeveRetornarRankingComPosicao() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<RankingProjection> paginaUsuarios = new PageImpl<>(List.of(usuario1, usuario2));
        
        when(usuarioRepository.findRankingSemanal(eq(PerfilUsuario.ALUNO), any(Pageable.class)))
                .thenReturn(paginaUsuarios);

        Page<RankingResponse> result = rankingService.listarRanking(pageRequest);

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
        PageRequest pageRequest = PageRequest.of(1, 10);
        Page<RankingProjection> paginaUsuarios = new PageImpl<>(List.of(usuario2));
        
        when(usuarioRepository.findRankingSemanal(eq(PerfilUsuario.ALUNO), any(Pageable.class)))
                .thenReturn(paginaUsuarios);

        Page<RankingResponse> result = rankingService.listarRanking(pageRequest);

        assertEquals(1, result.getContent().size());
        
        RankingResponse top1 = result.getContent().get(0);
        assertEquals(11, top1.posicao());
    }
}
