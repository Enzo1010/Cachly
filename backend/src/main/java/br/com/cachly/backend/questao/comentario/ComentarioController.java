package br.com.cachly.backend.questao.comentario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questoes")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping("/{id}/comentarios")
    public List<ComentarioResponse> listarPorQuestao(@PathVariable Long id) {
        return comentarioService.listarPorQuestao(id);
    }

    @PostMapping("/{id}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponse adicionarComentario(
            @PathVariable Long id,
            @Valid @RequestBody NovoComentarioRequest request) {
        return comentarioService.adicionarComentario(id, request);
    }

    @PostMapping("/comentarios/{id}/upvote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darUpvote(@PathVariable Long id) {
        comentarioService.darUpvote(id);
    }
}
