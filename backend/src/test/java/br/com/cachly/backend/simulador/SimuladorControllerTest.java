package br.com.cachly.backend.simulador;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimuladorController.class)
@ActiveProfiles("test")
class SimuladorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimuladorCacheService simuladorCacheService;

    @Test
    void deveExecutarSimulacaoERetornarStatusOk() throws Exception {
        SimulacaoResponse responseMock = new SimulacaoResponse(
                2, 2, 28, 4, 4, 2, 1, 1, 50.0, 50.0, List.of()
        );

        when(simuladorCacheService.executarSimulacao(any(SimulacaoRequest.class))).thenReturn(responseMock);

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bitsOffset").value(2))
                .andExpect(jsonPath("$.bitsIndice").value(2))
                .andExpect(jsonPath("$.bitsTag").value(28))
                .andExpect(jsonPath("$.totalLinhas").value(4))
                .andExpect(jsonPath("$.totalHits").value(1))
                .andExpect(jsonPath("$.totalMisses").value(1));
    }

    @Test
    void deveRetornarBadRequestQuandoPayloadInvalido() throws Exception {
        mockMvc.perform(post("/api/simulador/executar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tamanhoCacheBytes": -1,
                                  "tamanhoBlocoBytes": 4,
                                  "mapeamento": "DIRETO",
                                  "enderecos": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
