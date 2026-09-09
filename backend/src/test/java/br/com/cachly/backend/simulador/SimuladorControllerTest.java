package br.com.cachly.backend.simulador;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimuladorController.class)
@ActiveProfiles("test")
@Import(br.com.cachly.backend.seguranca.SecurityConfig.class)
class SimuladorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimuladorCacheService simuladorCacheService;

    @MockitoBean
    private br.com.cachly.backend.simulador.desafio.DesafioCacheService desafioCacheService;

    @MockitoBean
    private br.com.cachly.backend.seguranca.TokenService tokenService;

    @MockitoBean
    private br.com.cachly.backend.usuario.UsuarioRepository usuarioRepository;

    @MockitoBean
    private br.com.cachly.backend.usuario.UsuarioLogadoService usuarioLogadoService;

    @Test
    void deveListarDesafiosERetornarStatusOk() throws Exception {
        when(desafioCacheService.listarDesafios()).thenReturn(List.of());

        mockMvc.perform(get("/api/simulador/desafios")
                        .with(user("aluno").roles("ALUNO")))
                .andExpect(status().isOk());
    }

    @Test
    void deveExecutarSimulacaoERetornarStatusOk() throws Exception {
        SimulacaoResponse responseMock = new SimulacaoResponse(
                2, 2, 28, 4, 4, 2, 1, 1, 50.0, 50.0, List.of()
        );

        when(simuladorCacheService.executarSimulacao(any(SimulacaoRequest.class))).thenReturn(responseMock);

        mockMvc.perform(post("/api/simulador/executar")
                        .with(user("aluno").roles("ALUNO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tamanhoCacheBytes": 16,
                                  "tamanhoBlocoBytes": 4,
                                  "mapeamento": "DIRETO",
                                  "enderecos": [0, 4]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bitsOffset").value(2))
                .andExpect(jsonPath("$.bitsIndice").value(2))
                .andExpect(jsonPath("$.bitsTag").value(28))
                .andExpect(jsonPath("$.totalLinhas").value(4))
                .andExpect(jsonPath("$.totalHits").value(1))
                .andExpect(jsonPath("$.totalMisses").value(1));
    }

    @Test
    void deveVerificarDesafioERetornarStatusOk() throws Exception {
        br.com.cachly.backend.simulador.desafio.ResultadoDesafioResponse resultadoMock =
                new br.com.cachly.backend.simulador.desafio.ResultadoDesafioResponse(
                        true, "A", "Parabéns", 30,
                        new SimulacaoResponse(2, 2, 28, 4, 4, 1, 0, 1, 0.0, 100.0, List.of()),
                        1, "Estagiário", 30
                );

        when(desafioCacheService.verificarDesafio(any(), any(), any())).thenReturn(resultadoMock);

        mockMvc.perform(post("/api/simulador/desafios/cold-miss/verificar")
                        .with(user("aluno").roles("ALUNO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opcaoSelecionadaId": "A"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correto").value(true))
                .andExpect(jsonPath("$.opcaoCorretaId").value("A"))
                .andExpect(jsonPath("$.xpGanho").value(30));
    }

    @Test
    void deveRejeitarExecucaoSemAutenticacaoComStatus401() throws Exception {
        mockMvc.perform(post("/api/simulador/executar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tamanhoCacheBytes": 16,
                                  "tamanhoBlocoBytes": 4,
                                  "mapeamento": "DIRETO",
                                  "enderecos": [0, 4]
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
