package br.com.cachly.backend.usuario;

import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.CredenciaisInvalidasException;
import br.com.cachly.backend.seguranca.TokenService;
import br.com.cachly.backend.usuario.aluno.AlunoCadastroRequest;
import br.com.cachly.backend.usuario.aluno.AlunoResponse;
import br.com.cachly.backend.usuario.autenticacao.AutenticacaoRequest;
import br.com.cachly.backend.usuario.autenticacao.UsuarioAutenticadoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorSenha;
    private final TokenService tokenService;

    @Transactional
    public AlunoResponse cadastrarAluno(AlunoCadastroRequest request) {
        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoDeDadosException("Já existe um usuário com esse e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(codificadorSenha.encode(request.senha()));
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setXpTotal(0);
        usuario.setNivel(1);
        usuario.setAtivo(true);

        return converterParaResponse(usuarioRepository.save(usuario));
    }

    public UsuarioAutenticadoResponse autenticar(AutenticacaoRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(normalizarEmail(request.email()))
                .filter(Usuario::getAtivo)
                .filter(usuarioEncontrado -> codificadorSenha.matches(
                        request.senha(),
                        usuarioEncontrado.getSenhaHash()
                ))
                .orElseThrow(() -> new CredenciaisInvalidasException(
                        "E-mail ou senha inválidos"
                ));

        String token = tokenService.gerarToken(usuario);

        return new UsuarioAutenticadoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getXpTotal(),
                usuario.getNivel(),
                token
        );
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AlunoResponse converterParaResponse(Usuario usuario) {
        return new AlunoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getXpTotal(),
                usuario.getNivel(),
                usuario.getAtivo(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm()
        );
    }
}
