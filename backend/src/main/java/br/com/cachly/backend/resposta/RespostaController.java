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
    private final br.com.cachly.backend.usuario.UsuarioRepository usuarioRepository;

    @PostMapping("/{questaoId}/respostas")
    public RespostaResponse responder(
            @PathVariable Long questaoId,
            @Valid @RequestBody RespostaRequest request
    ) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException("Usuário não encontrado"));
        return respostaService.responder(questaoId, request, usuario);
    }
}
