package br.com.cachly.backend.resposta;

import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RespostaController.class)
@ActiveProfiles("test")
class RespostaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RespostaService respostaService;

    @BeforeEach
    void setUp() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(10L);
        usuarioMock.setNome("Aluno Teste");
        usuarioMock.setEmail("aluno@teste.com");
        usuarioMock.setPerfil(PerfilUsuario.ALUNO);

        var auth = new UsernamePasswordAuthenticationToken(usuarioMock, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void deveResponderQuestaoERetornarStatusOk() throws Exception {
        when(respostaService.responder(eq(1L), any(RespostaRequest.class), any(Usuario.class)))
                .thenReturn(new RespostaResponse(100L, true, "Explicação da questão", 10, 1, "Estagiário", 10));

        mockMvc.perform(post("/api/questoes/1/respostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "alternativaId": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tentativaId").value(100))
                .andExpect(jsonPath("$.correta").value(true))
                .andExpect(jsonPath("$.explicacao").value("Explicação da questão"))
                .andExpect(jsonPath("$.xpConcedido").value(10))
                .andExpect(jsonPath("$.nivelAtual").value(1))
                .andExpect(jsonPath("$.xpTotal").value(10));
    }

    @Test
    void deveRetornarNotFoundQuandoQuestaoOuAlternativaInexistente() throws Exception {
        when(respostaService.responder(eq(99L), any(RespostaRequest.class), any(Usuario.class)))
                .thenThrow(new RecursoNaoEncontradoException("Questão não encontrada"));

        mockMvc.perform(post("/api/questoes/99/respostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "alternativaId": 10
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Questão não encontrada"));
    }

    @Test
    void deveRecusarRequisicaoSemAlternativaId() throws Exception {
        mockMvc.perform(post("/api/questoes/1/respostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.alternativaId").exists());
    }
}
