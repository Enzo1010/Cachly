package br.com.questly.backend.seguranca;

import br.com.questly.backend.usuario.PerfilUsuario;
import br.com.questly.backend.usuario.Usuario;
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
    void deveRetornarNullParaTokenInvalido() {
        String email = tokenService.validarToken("token-invalido-qualquer");
        assertNull(email);
    }
}
