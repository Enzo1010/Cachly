package br.com.questly.backend.alternativa;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questoes/{questaoId}/alternativas")
@RequiredArgsConstructor
public class AlternativaController {

    private final AlternativaService alternativaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AlternativaResponse cadastrar(
            @PathVariable Long questaoId,
            @Valid @RequestBody AlternativaRequest request
    ) {
        return alternativaService.cadastrar(questaoId, request);
    }

    @GetMapping
    public List<AlternativaResponse> listarAtivas(@PathVariable Long questaoId) {
        return alternativaService.listarAtivas(questaoId);
    }

    @GetMapping("/{id}")
    public AlternativaResponse buscarPorId(
            @PathVariable Long questaoId,
            @PathVariable Long id
    ) {
        return alternativaService.buscarPorId(questaoId, id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AlternativaResponse atualizar(
            @PathVariable Long questaoId,
            @PathVariable Long id,
            @Valid @RequestBody AlternativaRequest request
    ) {
        return alternativaService.atualizar(questaoId, id, request);
    }

    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AlternativaResponse desativar(
            @PathVariable Long questaoId,
            @PathVariable Long id
    ) {
        return alternativaService.desativar(questaoId, id);
    }
}
