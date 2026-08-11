package br.com.questly.backend.usuario.aluno;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.questly.backend.usuario.Usuario;
import br.com.questly.backend.usuario.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlunoResponse cadastrar(@Valid @RequestBody AlunoCadastroRequest request) {
        return usuarioService.cadastrarAluno(request);
    }

    @GetMapping("/me/historico")
    public Page<HistoricoTentativaResponse> obterHistorico(
            @AuthenticationPrincipal Usuario usuario,
            Pageable pageable
    ) {
        Usuario usuarioAutenticado = usuario != null ? usuario :
                (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return alunoDesempenhoService.obterHistorico(usuarioAutenticado, pageable);
    }

    @GetMapping("/me/desempenho")
    public DesempenhoResponse obterDesempenho(
            @AuthenticationPrincipal Usuario usuario
    ) {
        Usuario usuarioAutenticado = usuario != null ? usuario :
                (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return alunoDesempenhoService.obterEstatisticas(usuarioAutenticado);
    }
}
