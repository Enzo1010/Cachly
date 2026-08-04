package br.com.questly.backend.categoria;

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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @Test
    void deveCadastrarCategoriaERetornarStatusCriado() throws Exception {
        when(categoriaService.cadastrar(any(CategoriaRequest.class)))
                .thenReturn(criarResponse(1L, "Álgebra Booleana", true));

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Álgebra Booleana",
                                  "descricao": "Operações booleanas"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Álgebra Booleana"))
                .andExpect(jsonPath("$.ativa").value(true));
    }

    @Test
    void deveListarCategoriasAtivasERetornarStatusOk() throws Exception {
        when(categoriaService.listarAtivas()).thenReturn(List.of(
                criarResponse(1L, "Álgebra Booleana", true),
                criarResponse(2L, "Portas Lógicas", true)
        ));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Álgebra Booleana"))
                .andExpect(jsonPath("$[1].nome").value("Portas Lógicas"));
    }

    @Test
    void deveBuscarCategoriaPorIdERetornarStatusOk() throws Exception {
        when(categoriaService.buscarPorId(1L))
                .thenReturn(criarResponse(1L, "Circuitos Digitais", true));

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Circuitos Digitais"));
    }

    @Test
    void deveAtualizarCategoriaERetornarStatusOk() throws Exception {
        when(categoriaService.atualizar(any(Long.class), any(CategoriaRequest.class)))
                .thenReturn(criarResponse(1L, "Circuitos Digitais", true));

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Circuitos Digitais",
                                  "descricao": "Conteúdo atualizado"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Circuitos Digitais"));
    }

    @Test
    void deveDesativarCategoriaERetornarStatusOk() throws Exception {
        when(categoriaService.desativar(1L))
                .thenReturn(criarResponse(1L, "Pipeline", false));

        mockMvc.perform(patch("/api/categorias/1/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ativa").value(false));
    }

    @Test
    void deveRetornarStatusInvalidoQuandoNomeEstiverVazio() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "",
                                  "descricao": "Descrição"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.nome").exists());
    }

    @Test
    void deveRetornarStatusNaoEncontradoQuandoCategoriaNaoExistir() throws Exception {
        when(categoriaService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException(
                        "Categoria não encontrada com o ID: 99"
                ));

        mockMvc.perform(get("/api/categorias/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem")
                        .value("Categoria não encontrada com o ID: 99"));
    }

    @Test
    void deveRetornarStatusConflitoQuandoNomeJaExistir() throws Exception {
        when(categoriaService.cadastrar(any(CategoriaRequest.class)))
                .thenThrow(new ConflitoDeDadosException(
                        "Já existe uma categoria com esse nome"
                ));

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Álgebra Booleana",
                                  "descricao": "Descrição"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem")
                        .value("Já existe uma categoria com esse nome"));
    }

    private CategoriaResponse criarResponse(Long id, String nome, boolean ativa) {
        OffsetDateTime agora = OffsetDateTime.now();
        return new CategoriaResponse(
                id,
                nome,
                "Descrição da categoria",
                ativa,
                agora,
                agora
        );
    }
}
