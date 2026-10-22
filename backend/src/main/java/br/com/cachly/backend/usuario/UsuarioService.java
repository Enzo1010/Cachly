package br.com.cachly.backend.usuario;

import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.CredenciaisInvalidasException;
import br.com.cachly.backend.resposta.XpService;
import br.com.cachly.backend.seguranca.TokenService;
import br.com.cachly.backend.usuario.aluno.AlunoCadastroRequest;
import br.com.cachly.backend.usuario.aluno.AlunoResponse;
import br.com.cachly.backend.usuario.autenticacao.AutenticacaoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import br.com.cachly.backend.config.EmailService;
import java.time.OffsetDateTime;
import java.util.UUID;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.usuario.autenticacao.UsuarioSessaoResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorSenha;
    private final TokenService tokenService;
    private final XpService xpService;
    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url:https://cachly.vercel.app}")
    private String frontendUrl;

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

    public record AuthResult(UsuarioSessaoResponse response, String token) {}

    public AuthResult autenticar(AutenticacaoRequest request) {
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

        return new AuthResult(new UsuarioSessaoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getXpTotal(),
                usuario.getNivel(),
                usuario.getDiasOfensiva(),
                xpService.nomeDoNivel(usuario.getNivel())
        ), token);
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
                xpService.nomeDoNivel(usuario.getNivel()),
                usuario.getAtivo(),
                usuario.getDiasOfensiva(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm()
        );
    }

    @Transactional
    public void alterarSenha(Long usuarioId, AlterarSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByIdForUpdate(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (!codificadorSenha.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException("Senha atual incorreta");
        }

        usuario.setSenhaHash(codificadorSenha.encode(request.novaSenha()));
        // CRÍTICO: Invalida todos os tokens JWT emitidos anteriormente
        usuario.setVersaoToken(System.currentTimeMillis());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void revogarTokens(Long usuarioId) {
        Usuario usuario = usuarioRepository.findByIdForUpdate(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        
        usuario.setVersaoToken(System.currentTimeMillis());
        usuarioRepository.save(usuario);
    }

    private String gerarHashSha256(String texto) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash do token", e);
        }
    }

    @Transactional
    public void solicitarResetSenha(String email) {
        usuarioRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .filter(Usuario::getAtivo)
                .ifPresent(usuario -> {
                    String token = UUID.randomUUID().toString();
                    // Segurança: Salva apenas o HASH do token no banco, nunca o token em texto plano
                    usuario.setTokenReset(gerarHashSha256(token));
                    usuario.setTokenResetExpiraEm(OffsetDateTime.now().plusMinutes(30));
                    usuarioRepository.save(usuario);

                    String linkReset = frontendUrl + "/resetar-senha?token=" + token;
                    emailService.enviarEmailResetSenha(usuario.getEmail(), usuario.getNome(), linkReset);
                });
        // Não lança exceção se o e-mail não existir (previne enumeração de e-mails)
    }

    @Transactional
    public void resetarSenha(ResetarSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByTokenReset(gerarHashSha256(request.token()))
                .filter(Usuario::getAtivo)
                .filter(u -> u.getTokenResetExpiraEm() != null && u.getTokenResetExpiraEm().isAfter(OffsetDateTime.now()))
                .orElseThrow(() -> new CredenciaisInvalidasException(
                        "Link inválido ou expirado. Solicite um novo link de redefinição."));

        usuario.setSenhaHash(codificadorSenha.encode(request.novaSenha()));
        usuario.setTokenReset(null);
        usuario.setTokenResetExpiraEm(null);
        usuario.setVersaoToken(System.currentTimeMillis());
        usuarioRepository.save(usuario);
    }
}
