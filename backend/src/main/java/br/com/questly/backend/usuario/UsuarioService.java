package br.com.questly.backend.usuario;

import br.com.questly.backend.comum.erro.ConflitoDeDadosException;
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
