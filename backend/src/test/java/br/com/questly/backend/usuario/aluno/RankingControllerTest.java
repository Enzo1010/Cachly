package br.com.questly.backend.usuario.aluno;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.questly.backend.seguranca.TokenService;
import br.com.questly.backend.usuario.UsuarioService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RankingController.class)
@ActiveProfiles("test")
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankingService rankingService;
    
    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void listarRanking_DeveRetornarRankingComSucesso() throws Exception {
        // Arrange
        RankingResponse r1 = new RankingResponse(1, "Enzo", 10, 500);
        RankingResponse r2 = new RankingResponse(2, "Alice", 8, 400);
        Page<RankingResponse> pagina = new PageImpl<>(List.of(r1, r2));

        when(rankingService.listarRanking(any(Pageable.class))).thenReturn(pagina);

        // Act & Assert
        mockMvc.perform(get("/api/ranking")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].posicao").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Enzo"))
                .andExpect(jsonPath("$.content[0].nivel").value(10))
                .andExpect(jsonPath("$.content[0].xpTotal").value(500))
                .andExpect(jsonPath("$.content[1].posicao").value(2))
                .andExpect(jsonPath("$.content[1].nome").value("Alice"));
    }
}
