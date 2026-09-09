package br.com.cachly.backend.usuario.autenticacao;

import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutenticacaoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder codificadorSenha;

    @Test
    void deveAutenticarUsuarioPersistidoSemExporSenha() throws Exception {
        String email = "login.integracao.%s@cachly.local".formatted(UUID.randomUUID());

        Usuario usuario = new Usuario();
        usuario.setNome("Teste de Integração");
        usuario.setEmail(email);
        usuario.setSenhaHash(codificadorSenha.encode("senha-segura"));
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setXpTotal(0);
        usuario.setNivel(1);
        usuario.setAtivo(true);
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "senha": "senha-segura"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Teste de Integração"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void deveConsultarDadosDaPropriaSessaoAutenticada() throws Exception {
        String email = "login.integracao.%s@cachly.local".formatted(UUID.randomUUID());

        Usuario usuario = new Usuario();
        usuario.setNome("Usuário Sessão Teste");
        usuario.setEmail(email);
        usuario.setSenhaHash(codificadorSenha.encode("senha-me-endpoint"));
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setXpTotal(120);
        usuario.setNivel(3);
        usuario.setAtivo(true);
        usuarioRepository.saveAndFlush(usuario);

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "senha": "senha-me-endpoint"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        jakarta.servlet.http.Cookie tokenCookie = result.getResponse().getCookie("token");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/me")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuario.getId()))
                .andExpect(jsonPath("$.nome").value("Usuário Sessão Teste"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.perfil").value("ALUNO"))
                .andExpect(jsonPath("$.xpTotal").value(120))
                .andExpect(jsonPath("$.nivel").value(3));
    }

    @Test
    void deveRevogarTokenAnteriorAposAlteracaoDeSenha() throws Exception {
        String email = "revogacao.integracao.%s@cachly.local".formatted(UUID.randomUUID());

        Usuario usuario = new Usuario();
        usuario.setNome("Usuário Revogação Teste");
        usuario.setEmail(email);
        usuario.setSenhaHash(codificadorSenha.encode("senha-antiga"));
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setXpTotal(0);
        usuario.setNivel(1);
        usuario.setAtivo(true);
        usuarioRepository.saveAndFlush(usuario);

        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "senha": "senha-antiga"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        jakarta.servlet.http.Cookie tokenCookie = loginResult.getResponse().getCookie("token");

        // Intervalo para garantir que a nova versaoToken no BD seja estritamente posterior ao iat do token T1
        Thread.sleep(1100);

        mockMvc.perform(post("/api/auth/alterar-senha")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "senha-antiga",
                                  "novaSenha": "nova-senha-123"
                                }
                                """))
                .andExpect(status().isOk());

        // Requisição subsequente utilizando o token T1 anterior deve ser rejeitada com HTTP 401
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/me")
                        .cookie(tokenCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveIgnorarTentativaDeElevacaoDePrivilegioNoCadastroPublico() throws Exception {
        String email = "hacker.admin.%s@cachly.local".formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Hacker Querendo Admin",
                                  "email": "%s",
                                  "senha": "senha-segura-123",
                                  "perfil": "ADMINISTRADOR"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("ALUNO"));

        Usuario usuarioCriado = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(PerfilUsuario.ALUNO, usuarioCriado.getPerfil());
    }
}
