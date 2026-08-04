package br.com.questly.backend.usuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
