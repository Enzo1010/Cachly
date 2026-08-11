package br.com.cachly.backend.usuario.autenticacao;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.cachly.backend.usuario.UsuarioService;
import br.com.cachly.backend.usuario.Usuario;
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
            @Valid @RequestBody AutenticacaoRequest request
    ) {
        return usuarioService.autenticar(request);
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
                usuario.getNivel()
        );
    }
}
