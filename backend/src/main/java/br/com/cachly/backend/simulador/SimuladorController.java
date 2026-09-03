package br.com.cachly.backend.simulador;

import br.com.cachly.backend.simulador.desafio.DesafioCacheResponse;
import br.com.cachly.backend.simulador.desafio.DesafioCacheService;
import br.com.cachly.backend.simulador.desafio.ResultadoDesafioResponse;
import br.com.cachly.backend.simulador.desafio.VerificarDesafioRequest;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioLogadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/simulador")
@RequiredArgsConstructor
public class SimuladorController {

    private final SimuladorCacheService simuladorCacheService;
    private final DesafioCacheService desafioCacheService;
    private final UsuarioLogadoService usuarioLogadoService;

    @PostMapping("/executar")
    public SimulacaoResponse executar(@Valid @RequestBody SimulacaoRequest request) {
        return simuladorCacheService.executarSimulacao(request);
    }

    @GetMapping("/desafios")
    public List<DesafioCacheResponse> listarDesafios() {
        return desafioCacheService.listarDesafios();
    }

    @GetMapping("/desafios/{id}")
    public DesafioCacheResponse obterDesafio(@PathVariable String id) {
        return desafioCacheService.obterDesafio(id);
    }

    @PostMapping("/desafios/{id}/verificar")
    public ResultadoDesafioResponse verificarDesafio(
            @PathVariable String id,
            @Valid @RequestBody VerificarDesafioRequest request
    ) {
        Usuario usuario = null;
        try {
            usuario = usuarioLogadoService.obterUsuarioAtual();
        } catch (Exception ignored) {
            // Permite responder e simular mesmo se o aluno não estiver com sessão válida no momento
        }
        return desafioCacheService.verificarDesafio(id, request, usuario);
    }
}
