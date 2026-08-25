package br.com.cachly.backend.usuario.aluno;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioLogadoService;
import br.com.cachly.backend.usuario.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final UsuarioService usuarioService;
    private final AlunoDesempenhoService alunoDesempenhoService;
    private final UsuarioLogadoService usuarioLogadoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlunoResponse cadastrar(@Valid @RequestBody AlunoCadastroRequest request) {
        return usuarioService.cadastrarAluno(request);
    }

    @GetMapping("/me/historico")
    public Page<HistoricoTentativaResponse> obterHistorico(
            Pageable pageable
    ) {
        Usuario usuario = usuarioLogadoService.obterUsuarioAtual();
        return alunoDesempenhoService.obterHistorico(usuario, pageable);
    }

    @GetMapping("/me/desempenho")
    public DesempenhoResponse obterDesempenho() {
        Usuario usuario = usuarioLogadoService.obterUsuarioAtual();
        return alunoDesempenhoService.obterEstatisticas(usuario);
    }
}
