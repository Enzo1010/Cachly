package br.com.cachly.backend.usuario.autenticacao;

import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioLogadoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.cachly.backend.usuario.UsuarioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final UsuarioService usuarioService;
    private final UsuarioLogadoService usuarioLogadoService;

    @PostMapping("/login")
    public UsuarioSessaoResponse autenticar(
            @Valid @RequestBody AutenticacaoRequest request,
            HttpServletResponse response
    ) {
        br.com.cachly.backend.usuario.UsuarioService.AuthResult authResult = usuarioService.autenticar(request);
        
        long maxAge = Boolean.TRUE.equals(request.lembrarLogin()) ? 30 * 24 * 60 * 60 : 24 * 60 * 60;

        ResponseCookie cookie = ResponseCookie.from("token", authResult.token())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
                
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return authResult.response();
    }
    
    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
                
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @GetMapping("/me")
    public UsuarioSessaoResponse obterUsuarioAutenticado() {
        return usuarioLogadoService.obterSessaoAtual();
    }

    @PostMapping("/alterar-senha")
    public void alterarSenha(@Valid @RequestBody br.com.cachly.backend.usuario.AlterarSenhaRequest request) {
        Usuario usuario = usuarioLogadoService.obterUsuarioAtual();
        usuarioService.alterarSenha(usuario.getId(), request);
    }

    @PostMapping("/revogar-sessoes")
    public void revogarSessoes() {
        Usuario usuario = usuarioLogadoService.obterUsuarioAtual();
        usuarioService.revogarTokens(usuario.getId());
    }
}

