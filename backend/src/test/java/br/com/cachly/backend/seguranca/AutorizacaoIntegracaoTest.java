package br.com.cachly.backend.seguranca;

import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutorizacaoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder codificadorSenha;

    @Autowired
    private TokenService tokenService;

    private String tokenAluno;

    @BeforeEach
    void setUp() {
        String email = "aluno.%s@cachly.local".formatted(UUID.randomUUID());
        Usuario aluno = new Usuario();
        aluno.setNome("Aluno Teste RBAC");
        aluno.setEmail(email);
        aluno.setSenhaHash(codificadorSenha.encode("senha-segura"));
        aluno.setPerfil(PerfilUsuario.ALUNO);
        aluno.setXpTotal(0);
        aluno.setNivel(1);
        aluno.setAtivo(true);
        usuarioRepository.saveAndFlush(aluno);

        tokenAluno = tokenService.gerarToken(aluno);
    }

    @Test
    void alunoNaoDeveCadastrarCategoria() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Categoria Teste",
                                  "descricao": "Desc"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveAtualizarCategoria() throws Exception {
        mockMvc.perform(put("/api/categorias/1")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Categoria Atualizada",
                                  "descricao": "Desc"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveDesativarCategoria() throws Exception {
        mockMvc.perform(patch("/api/categorias/1/desativar")
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveCadastrarQuestao() throws Exception {
        mockMvc.perform(post("/api/questoes")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Teste",
                                  "enunciado": "Enunciado",
                                  "dificuldade": "FACIL",
                                  "categoriaId": 1,
                                  "tipo": "MULTIPLA_ESCOLHA",
                                  "xpBase": 10,
                                  "explicacao": "Explicacao"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveAtualizarQuestao() throws Exception {
        mockMvc.perform(put("/api/questoes/1")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Teste",
                                  "enunciado": "Enunciado",
                                  "dificuldade": "FACIL",
                                  "categoriaId": 1,
                                  "tipo": "MULTIPLA_ESCOLHA",
                                  "xpBase": 10,
                                  "explicacao": "Explicacao"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveDesativarQuestao() throws Exception {
        mockMvc.perform(patch("/api/questoes/1/desativar")
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveCadastrarAlternativa() throws Exception {
        mockMvc.perform(post("/api/questoes/1/alternativas")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "texto": "Teste",
                                  "correta": true,
                                  "justificativa": "Justificativa",
                                  "ordem": 1
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveAtualizarAlternativa() throws Exception {
        mockMvc.perform(put("/api/questoes/1/alternativas/1")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "texto": "Teste",
                                  "correta": true,
                                  "justificativa": "Justificativa",
                                  "ordem": 1
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveDesativarAlternativa() throws Exception {
        mockMvc.perform(patch("/api/questoes/1/alternativas/1/desativar")
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isForbidden());
    }
}
