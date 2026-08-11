package br.com.cachly.backend.resposta;

import br.com.cachly.backend.usuario.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PostMapping("/{questaoId}/respostas")
    public RespostaResponse responder(
            @PathVariable Long questaoId,
            @Valid @RequestBody RespostaRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        Usuario usuarioAutenticado = usuario != null ? usuario :
                (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return respostaService.responder(questaoId, request, usuarioAutenticado);
    }
}
