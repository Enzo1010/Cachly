package br.com.questly.backend.alternativa;

import br.com.questly.backend.comum.erro.ConflitoDeDadosException;
import br.com.questly.backend.comum.erro.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlternativaController.class)
class AlternativaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlternativaService alternativaService;

    @Test
    void deveCadastrarAlternativaERetornarStatusCriado() throws Exception {
        when(alternativaService.cadastrar(eq(1L), any(AlternativaRequest.class)))
                .thenReturn(criarResponse(10L, true));

        mockMvc.perform(post("/api/questoes/1/alternativas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.questaoId").value(1))
                .andExpect(jsonPath("$.correta").value(true))
                .andExpect(jsonPath("$.ordem").value(1))
                .andExpect(jsonPath("$.ativa").value(true));
    }

    @Test
    void deveListarAlternativasAtivasERetornarStatusOk() throws Exception {
        when(alternativaService.listarAtivas(1L)).thenReturn(List.of(
                criarResponse(10L, true),
                criarResponse(11L, true)
        ));

        mockMvc.perform(get("/api/questoes/1/alternativas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[1].id").value(11));
    }

    @Test
    void deveBuscarAlternativaPorIdERetornarStatusOk() throws Exception {
        when(alternativaService.buscarPorId(1L, 10L))
                .thenReturn(criarResponse(10L, true));

        mockMvc.perform(get("/api/questoes/1/alternativas/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.texto").value("Saída igual a 1"));
    }

    @Test
    void deveAtualizarAlternativaERetornarStatusOk() throws Exception {
        when(alternativaService.atualizar(
                eq(1L),
                eq(10L),
                any(AlternativaRequest.class)
        )).thenReturn(criarResponse(10L, true));

        mockMvc.perform(put("/api/questoes/1/alternativas/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.questaoId").value(1));
    }

    @Test
    void deveDesativarAlternativaERetornarStatusOk() throws Exception {
        when(alternativaService.desativar(1L, 10L))
                .thenReturn(criarResponse(10L, false));

        mockMvc.perform(patch("/api/questoes/1/alternativas/10/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.ativa").value(false));
    }

    @Test
    void deveRetornarRequisicaoInvalidaQuandoCamposForemInvalidos() throws Exception {
        mockMvc.perform(post("/api/questoes/1/alternativas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "texto": "",
                                  "correta": null,
                                  "ordem": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.texto").exists())
                .andExpect(jsonPath("$.campos.correta").exists())
                .andExpect(jsonPath("$.campos.ordem").exists());
    }

    @Test
    void deveRetornarStatusNaoEncontradoQuandoQuestaoNaoExistir() throws Exception {
        when(alternativaService.listarAtivas(99L))
                .thenThrow(new RecursoNaoEncontradoException(
                        "Questão não encontrada com o ID: 99"
                ));

        mockMvc.perform(get("/api/questoes/99/alternativas"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem")
                        .value("Questão não encontrada com o ID: 99"));
    }

    @Test
    void deveRetornarStatusConflitoQuandoOrdemJaExistir() throws Exception {
        when(alternativaService.cadastrar(eq(1L), any(AlternativaRequest.class)))
                .thenThrow(new ConflitoDeDadosException(
                        "Já existe uma alternativa com essa ordem para a questão"
                ));

        mockMvc.perform(post("/api/questoes/1/alternativas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe uma alternativa com essa ordem para a questão"
                ));
    }

    private AlternativaResponse criarResponse(Long id, boolean ativa) {
        OffsetDateTime agora = OffsetDateTime.now();
        return new AlternativaResponse(
                id,
                1L,
                "Saída igual a 1",
                true,
                (short) 1,
                ativa,
                agora,
                agora
        );
    }

    private String criarJsonValido() {
        return """
                {
                  "texto": "Saída igual a 1",
                  "correta": true,
                  "ordem": 1
                }
                """;
    }
}
