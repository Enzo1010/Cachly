package br.com.questly.backend.questao;

import br.com.questly.backend.comum.erro.ConflitoDeDadosException;
import br.com.questly.backend.comum.erro.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
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

@WebMvcTest(QuestaoController.class)
@ActiveProfiles("test")
class QuestaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestaoService questaoService;

    @Test
    void deveCadastrarQuestaoERetornarStatusCriado() throws Exception {
        when(questaoService.cadastrar(any(QuestaoRequest.class)))
                .thenReturn(criarResponse(1L, true));

        mockMvc.perform(post("/api/questoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoriaId").value(2))
                .andExpect(jsonPath("$.dificuldade").value("FACIL"))
                .andExpect(jsonPath("$.xpBase").value(10))
                .andExpect(jsonPath("$.ativa").value(true));
    }

    @Test
    void deveListarQuestoesAtivasERetornarStatusOk() throws Exception {
        when(questaoService.listarAtivas()).thenReturn(List.of(
                criarResponse(1L, true),
                criarResponse(2L, true)
        ));

        mockMvc.perform(get("/api/questoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void deveBuscarQuestaoPorIdERetornarStatusOk() throws Exception {
        when(questaoService.buscarPorId(1L)).thenReturn(criarResponse(1L, true));

        mockMvc.perform(get("/api/questoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.enunciado").value("O que é uma porta AND?"));
    }

    @Test
    void deveAtualizarQuestaoERetornarStatusOk() throws Exception {
        when(questaoService.atualizar(eq(1L), any(QuestaoRequest.class)))
                .thenReturn(criarResponse(1L, true));

        mockMvc.perform(put("/api/questoes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoriaNome").value("Portas Lógicas"));
    }

    @Test
    void deveDesativarQuestaoERetornarStatusOk() throws Exception {
        when(questaoService.desativar(1L)).thenReturn(criarResponse(1L, false));

        mockMvc.perform(patch("/api/questoes/1/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ativa").value(false));
    }

    @Test
    void deveRetornarRequisicaoInvalidaQuandoCamposForemInvalidos() throws Exception {
        mockMvc.perform(post("/api/questoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": null,
                                  "enunciado": "",
                                  "explicacao": "",
                                  "dificuldade": null,
                                  "xpBase": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.categoriaId").exists())
                .andExpect(jsonPath("$.campos.enunciado").exists())
                .andExpect(jsonPath("$.campos.explicacao").exists())
                .andExpect(jsonPath("$.campos.dificuldade").exists())
                .andExpect(jsonPath("$.campos.xpBase").exists());
    }

    @Test
    void deveRetornarStatusNaoEncontradoQuandoQuestaoNaoExistir() throws Exception {
        when(questaoService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException(
                        "Questão não encontrada com o ID: 99"
                ));

        mockMvc.perform(get("/api/questoes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem")
                        .value("Questão não encontrada com o ID: 99"));
    }

    @Test
    void deveRetornarStatusConflitoQuandoCategoriaEstiverInativa() throws Exception {
        when(questaoService.cadastrar(any(QuestaoRequest.class)))
                .thenThrow(new ConflitoDeDadosException(
                        "A categoria informada está inativa"
                ));

        mockMvc.perform(post("/api/questoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem")
                        .value("A categoria informada está inativa"));
    }

    private QuestaoResponse criarResponse(Long id, boolean ativa) {
        OffsetDateTime agora = OffsetDateTime.now();
        return new QuestaoResponse(
                id,
                2L,
                "Portas Lógicas",
                "O que é uma porta AND?",
                "Uma porta que realiza conjunção lógica.",
                DificuldadeQuestao.FACIL,
                10,
                ativa,
                agora,
                agora
        );
    }

    private String criarJsonValido() {
        return """
                {
                  "categoriaId": 2,
                  "enunciado": "O que é uma porta AND?",
                  "explicacao": "Uma porta que realiza conjunção lógica.",
                  "dificuldade": "FACIL",
                  "xpBase": 10
                }
                """;
    }
}
