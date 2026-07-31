package br.com.questly.backend.controller;

import br.com.questly.backend.dto.CategoriaRequest;
import br.com.questly.backend.dto.CategoriaResponse;
import br.com.questly.backend.service.CategoriaService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse cadastrar(@Valid @RequestBody CategoriaRequest request) {
        return categoriaService.cadastrar(request);
    }

    @GetMapping
    public List<CategoriaResponse> listarAtivas() {
        return categoriaService.listarAtivas();
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscarPorId(@PathVariable Long id) {
        return categoriaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request
    ) {
        return categoriaService.atualizar(id, request);
    }

    @PatchMapping("/{id}/desativar")
    public CategoriaResponse desativar(@PathVariable Long id) {
        return categoriaService.desativar(id);
    }
}
