package br.com.cachly.backend.usuario.autenticacao;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.cachly.backend.usuario.UsuarioService;
import br.com.cachly.backend.usuario.Usuario;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PostMapping("/login")
    public UsuarioAutenticadoResponse autenticar(
            @Valid @RequestBody AutenticacaoRequest request,
            HttpServletResponse response
    ) {
        UsuarioAutenticadoResponse authResponse = usuarioService.autenticar(request);
        
        ResponseCookie cookie = ResponseCookie.from("token", authResponse.token())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(24 * 60 * 60)
                .build();
                
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return authResponse;
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
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new UsuarioSessaoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getXpTotal(),
                usuario.getNivel(),
                usuario.getDiasOfensiva()
        );
    }
}
