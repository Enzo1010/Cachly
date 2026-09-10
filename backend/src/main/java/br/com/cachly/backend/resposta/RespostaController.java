package br.com.cachly.backend.resposta;

import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioLogadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questoes")
@RequiredArgsConstructor
public class RespostaController {

    private final RespostaService respostaService;
    private final UsuarioLogadoService usuarioLogadoService;

    @PostMapping("/{questaoId}/respostas")
    @PreAuthorize("hasRole('ALUNO')")
    public RespostaResponse responder(
            @PathVariable Long questaoId,
            @Valid @RequestBody RespostaRequest request
    ) {
        Usuario usuario = usuarioLogadoService.obterUsuarioAtual();
        return respostaService.responder(questaoId, request, usuario);
    }
}
