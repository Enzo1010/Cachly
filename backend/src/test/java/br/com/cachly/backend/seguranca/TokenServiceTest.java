package br.com.cachly.backend.seguranca;

import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "segredo-para-testes-com-tamanho-suficiente-para-hmac");
        ReflectionTestUtils.setField(tokenService, "expirationHours", 24);
        ReflectionTestUtils.setField(tokenService, "rememberExpirationHours", 720);
        ReflectionTestUtils.invokeMethod(tokenService, "init");
    }

    @Test
    void deveGerarValidadeETokenValido() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana Silva");
        usuario.setEmail("ana.silva@exemplo.com");
        usuario.setPerfil(PerfilUsuario.ALUNO);

        String token = tokenService.gerarToken(usuario);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        String email = tokenService.validarToken(token);
        assertEquals("ana.silva@exemplo.com", email);
    }

    @Test
    void deveAlinharExpiracaoComLembrarLogin() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("aluno@teste.com");
        usuario.setPerfil(PerfilUsuario.ALUNO);
        var normal = tokenService.extrairClaims(tokenService.gerarToken(usuario));
        var lembrado = tokenService.extrairClaims(tokenService.gerarToken(usuario, true));
        assertEquals(24 * 3600000L, normal.getExpiration().getTime() - normal.getIssuedAt().getTime());
        assertEquals(720 * 3600000L, lembrado.getExpiration().getTime() - lembrado.getIssuedAt().getTime());
    }

    @Test
    void deveRetornarNullParaTokenInvalido() {
        String email = tokenService.validarToken("token-invalido-qualquer");
        assertNull(email);
    }
}
