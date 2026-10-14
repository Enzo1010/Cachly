package br.com.cachly.backend.simulador.desafio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/desafios-simulador")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminDesafioSimuladorController {

    private final AdminDesafioSimuladorService adminService;

    @GetMapping
    public List<DesafioCacheResponse> listar() {
        return adminService.listarTodos();
    }

    @GetMapping("/{id}")
    public DesafioCacheResponse obterPorId(@PathVariable String id) {
        return adminService.obterPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DesafioCacheResponse cadastrar(@Valid @RequestBody DesafioSimuladorRequest request) {
        return adminService.cadastrar(request);
    }

    @PutMapping("/{id}")
    public DesafioCacheResponse atualizar(@PathVariable String id, @Valid @RequestBody DesafioSimuladorRequest request) {
        return adminService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String id) {
        adminService.excluir(id);
    }
}
