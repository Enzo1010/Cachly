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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
        mockMvc.perform(post("/api/categorias").with(csrf())
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
        mockMvc.perform(put("/api/categorias/1").with(csrf())
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
        mockMvc.perform(patch("/api/categorias/1/desativar").with(csrf())
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveCadastrarQuestao() throws Exception {
        mockMvc.perform(post("/api/questoes").with(csrf())
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
                                  "explicacao": "Explicacao",
                                  "alternativas": [
                                    { "id": null, "texto": "Alt 1", "correta": true, "ordem": 1 },
                                    { "id": null, "texto": "Alt 2", "correta": false, "ordem": 2 }
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveAtualizarQuestao() throws Exception {
        mockMvc.perform(put("/api/questoes/1").with(csrf())
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
                                  "explicacao": "Explicacao",
                                  "alternativas": [
                                    { "id": null, "texto": "Alt 1", "correta": true, "ordem": 1 },
                                    { "id": null, "texto": "Alt 2", "correta": false, "ordem": 2 }
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoDeveDesativarQuestao() throws Exception {
        mockMvc.perform(patch("/api/questoes/1/desativar").with(csrf())
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar401SeUsuarioEstiverDesativado() throws Exception {
        // 1. Antes da desativação, o token funciona (retorna 200 OK no endpoint /api/auth/me)
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/me")
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isOk());

        // 2. Desativar o usuário no banco de dados (simulando uma ação de admin)
        String emailDoAluno = tokenService.extrairClaims(tokenAluno).getSubject();
        Usuario alunoDesativado = usuarioRepository.findByEmailIgnoreCase(emailDoAluno).orElseThrow();
        alunoDesativado.setAtivo(false);
        usuarioRepository.saveAndFlush(alunoDesativado);

        // 3. Tentar acessar com o MESMO token após a desativação.
        // A query no SecurityFilter deve notar que o usuário não está mais ativo,
        // falhar a autenticação silenciosamente e o Spring Security retornará 401.
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/me")
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isUnauthorized());
    }
}
