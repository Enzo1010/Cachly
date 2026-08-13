package br.com.cachly.backend.simulador;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulador")
@RequiredArgsConstructor
public class SimuladorController {

    private final SimuladorCacheService simuladorCacheService;

    @PostMapping("/executar")
    public SimulacaoResponse executar(@Valid @RequestBody SimulacaoRequest request) {
        return simuladorCacheService.executarSimulacao(request);
    }
}
