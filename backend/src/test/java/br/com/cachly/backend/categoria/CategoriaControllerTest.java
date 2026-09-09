package br.com.cachly.backend.categoria;

import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@ActiveProfiles("test")
@Import(br.com.cachly.backend.seguranca.SecurityConfig.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private br.com.cachly.backend.seguranca.TokenService tokenService;

    @MockitoBean
    private br.com.cachly.backend.usuario.UsuarioRepository usuarioRepository;

    @Test
    void deveCadastrarCategoriaERetornarStatusCriado() throws Exception {
        when(categoriaService.cadastrar(any(CategoriaRequest.class)))
                .thenReturn(criarResponse(1L, "Álgebra Booleana", true));

        mockMvc.perform(post("/api/categorias")
                        .with(user("admin").roles("ADMINISTRADOR"))
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

        mockMvc.perform(get("/api/categorias")
                        .with(user("aluno").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Álgebra Booleana"))
                .andExpect(jsonPath("$[1].nome").value("Portas Lógicas"));
    }

    @Test
    void deveBuscarCategoriaPorIdERetornarStatusOk() throws Exception {
        when(categoriaService.buscarPorId(1L))
                .thenReturn(criarResponse(1L, "Circuitos Digitais", true));

        mockMvc.perform(get("/api/categorias/1")
                        .with(user("aluno").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Circuitos Digitais"));
    }

    @Test
    void deveAtualizarCategoriaERetornarStatusOk() throws Exception {
        when(categoriaService.atualizar(any(Long.class), any(CategoriaRequest.class)))
                .thenReturn(criarResponse(1L, "Circuitos Digitais", true));

        mockMvc.perform(put("/api/categorias/1")
                        .with(user("admin").roles("ADMINISTRADOR"))
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

        mockMvc.perform(patch("/api/categorias/1/desativar")
                        .with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ativa").value(false));
    }

    @Test
    void deveRetornarStatusInvalidoQuandoNomeEstiverVazio() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .with(user("admin").roles("ADMINISTRADOR"))
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

        mockMvc.perform(get("/api/categorias/99")
                        .with(user("aluno").roles("ALUNO")))
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
                        .with(user("admin").roles("ADMINISTRADOR"))
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

    @Test
    void deveRejeitarListagemSemAutenticacaoComStatus401() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarCadastroSemAutenticacaoComStatus401() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Álgebra Booleana",
                                  "descricao": "Operações booleanas"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarCadastroComPerfilAlunoComStatus403() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .with(user("aluno").roles("ALUNO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Álgebra Booleana",
                                  "descricao": "Operações booleanas"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarAtualizacaoComPerfilAlunoComStatus403() throws Exception {
        mockMvc.perform(put("/api/categorias/1")
                        .with(user("aluno").roles("ALUNO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Circuitos Digitais",
                                  "descricao": "Conteúdo atualizado"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarDesativacaoComPerfilAlunoComStatus403() throws Exception {
        mockMvc.perform(patch("/api/categorias/1/desativar")
                        .with(user("aluno").roles("ALUNO")))
                .andExpect(status().isForbidden());
    }

    private CategoriaResponse criarResponse(Long id, String nome, boolean ativa) {
        OffsetDateTime agora = OffsetDateTime.now();
        return new CategoriaResponse(
                id,
                nome,
                "Descrição da categoria",
                ativa,
                agora,
                agora,
                null,
                null
        );
    }
}
