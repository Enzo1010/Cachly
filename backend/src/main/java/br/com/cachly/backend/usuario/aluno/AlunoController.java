package br.com.cachly.backend.usuario.aluno;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioService;
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
    private final br.com.cachly.backend.usuario.UsuarioRepository usuarioRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlunoResponse cadastrar(@Valid @RequestBody AlunoCadastroRequest request) {
        return usuarioService.cadastrarAluno(request);
    }

    @GetMapping("/me/historico")
    public Page<HistoricoTentativaResponse> obterHistorico(
            Pageable pageable
    ) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException("Usuário não encontrado"));
        return alunoDesempenhoService.obterHistorico(usuario, pageable);
    }

    @GetMapping("/me/desempenho")
    public DesempenhoResponse obterDesempenho() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException("Usuário não encontrado"));
        return alunoDesempenhoService.obterEstatisticas(usuario);
    }
}
