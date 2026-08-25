package br.com.cachly.backend.usuario;

import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioLogadoService {

    private final UsuarioRepository usuarioRepository;
    private final HttpServletRequest request;

    public Usuario obterUsuarioAtual() {
        // Caching por request para não fazer N queries se chamado várias vezes
        Usuario usuarioCacheado = (Usuario) request.getAttribute("USUARIO_LOGADO_CACHE");
        if (usuarioCacheado != null) {
            return usuarioCacheado;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof br.com.cachly.backend.seguranca.UsuarioPrincipal)) {
            throw new br.com.cachly.backend.comum.erro.CredenciaisInvalidasException("Usuário não autenticado");
        }

        br.com.cachly.backend.seguranca.UsuarioPrincipal principal = (br.com.cachly.backend.seguranca.UsuarioPrincipal) auth.getPrincipal();
        String email = principal.email();
        Long iat = principal.iat();

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        // Se o token foi emitido (iat) ANTES da última atualização da credencial, recusa-o.
        // Convertendo de milissegundos para facilitar, permitimos uma folga de 1000ms.
        if (iat < (usuario.getVersaoToken() - 1000)) {
            throw new br.com.cachly.backend.comum.erro.CredenciaisInvalidasException("Sessão expirada. Por favor, faça login novamente.");
        }

        request.setAttribute("USUARIO_LOGADO_CACHE", usuario);
        return usuario;
    }
}
