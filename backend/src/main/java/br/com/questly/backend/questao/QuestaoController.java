package br.com.questly.backend.questao;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questoes")
@RequiredArgsConstructor
public class QuestaoController {

    private final QuestaoService questaoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestaoResponse cadastrar(@Valid @RequestBody QuestaoRequest request) {
        return questaoService.cadastrar(request);
    }

    @GetMapping
    public List<QuestaoResponse> listarAtivas() {
        return questaoService.listarAtivas();
    }

    @GetMapping("/estudo")
    public List<QuestaoEstudoResponse> listarParaEstudo(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false, defaultValue = "10") Integer limite
    ) {
        return questaoService.listarParaEstudo(categoriaId, limite);
    }

    @GetMapping("/{id}")
    public QuestaoResponse buscarPorId(@PathVariable Long id) {
        return questaoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public QuestaoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody QuestaoRequest request
    ) {
        return questaoService.atualizar(id, request);
    }

    @PatchMapping("/{id}/desativar")
    public QuestaoResponse desativar(@PathVariable Long id) {
        return questaoService.desativar(id);
    }
}
