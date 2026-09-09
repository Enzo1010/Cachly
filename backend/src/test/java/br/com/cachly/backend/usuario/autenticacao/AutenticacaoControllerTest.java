package br.com.cachly.backend.usuario.autenticacao;

import br.com.cachly.backend.usuario.Usuario;

import br.com.cachly.backend.comum.erro.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import br.com.cachly.backend.usuario.UsuarioService;
import br.com.cachly.backend.usuario.PerfilUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutenticacaoController.class)
@ActiveProfiles("test")
@Import(br.com.cachly.backend.seguranca.SecurityConfig.class)
class AutenticacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private br.com.cachly.backend.seguranca.TokenService tokenService;

    @MockitoBean
    private br.com.cachly.backend.usuario.UsuarioRepository usuarioRepository;

    @MockitoBean
    private br.com.cachly.backend.usuario.UsuarioLogadoService usuarioLogadoService;

    @Test
    void deveRetornarUsuarioAutenticado() throws Exception {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setNome("Ana Silva");
        usuarioMock.setEmail("ana.silva@exemplo.com");
        usuarioMock.setPerfil(PerfilUsuario.ALUNO);
        usuarioMock.setXpTotal(0);
        usuarioMock.setNivel(1);

        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                usuarioMock.getEmail(), null, java.util.List.of()
        );
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        when(usuarioRepository.findByEmailIgnoreCase("ana.silva@exemplo.com")).thenReturn(java.util.Optional.of(usuarioMock));
        when(usuarioLogadoService.obterSessaoAtual()).thenReturn(new br.com.cachly.backend.usuario.autenticacao.UsuarioSessaoResponse(
                        1L, "Ana Silva", "ana.silva@exemplo.com", br.com.cachly.backend.usuario.PerfilUsuario.ALUNO, 0, 1, 0, "Estagiário"));

        mockMvc.perform(get("/api/auth/me")
                        .with(user("ana.silva@exemplo.com").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value("ana.silva@exemplo.com"))
                .andExpect(jsonPath("$.perfil").value("ALUNO"))
                .andExpect(jsonPath("$.xpTotal").value(0))
                .andExpect(jsonPath("$.nivel").value(1));

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void deveRejeitarAcessoAoMeSemAutenticacaoComStatus401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() throws Exception {
        when(usuarioService.autenticar(any(AutenticacaoRequest.class)))
                .thenReturn(new br.com.cachly.backend.usuario.UsuarioService.AuthResult(new br.com.cachly.backend.usuario.autenticacao.UsuarioSessaoResponse(
                        1L,
                        "Ana Silva",
                        "ana.silva@exemplo.com",
                        br.com.cachly.backend.usuario.PerfilUsuario.ALUNO,
                        0,
                        1,
                        0,
                        "Estagiário"
                ), "falso-jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ana.silva@exemplo.com",
                                  "senha": "senha-segura"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value("ana.silva@exemplo.com"))
                .andExpect(jsonPath("$.perfil").value("ALUNO"))
                .andExpect(jsonPath("$.xpTotal").value(0))
                .andExpect(jsonPath("$.nivel").value(1))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void deveRecusarCredenciaisInvalidas() throws Exception {
        when(usuarioService.autenticar(any(AutenticacaoRequest.class)))
                .thenThrow(new CredenciaisInvalidasException(
                        "E-mail ou senha inválidos"
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ana.silva@exemplo.com",
                                  "senha": "senha-incorreta"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));
    }

    @Test
    void deveRecusarCamposObrigatoriosInvalidos() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "email-invalido",
                                  "senha": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }
}

