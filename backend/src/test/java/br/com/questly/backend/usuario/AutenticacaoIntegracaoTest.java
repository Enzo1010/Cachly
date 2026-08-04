package br.com.questly.backend.usuario;

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
        String email = "login.integracao.%s@questly.local".formatted(UUID.randomUUID());

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
}
