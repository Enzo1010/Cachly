package br.com.cachly.backend.seguranca;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de segurança quase-stateless: constrói o contexto de autenticação
 * a partir das claims do JWT, mas faz uma validação ultra-rápida (projection)
 * no banco de dados para garantir que a sessão (versão do token) não foi revogada.
 *
 * <p>Claims utilizadas (injetadas em {@link TokenService#gerarToken}):
 * <ul>
 *   <li>{@code sub} — e-mail do usuário</li>
 *   <li>{@code id}  — ID numérico do usuário</li>
 *   <li>{@code perfil} — nome do enum de perfil (ex.: "ADMIN", "USUARIO")</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final br.com.cachly.backend.usuario.UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = recuperarToken(request);

        if (token != null) {
            try {
                Claims claims = tokenService.extrairClaims(token);

                String email  = claims.getSubject();
                String perfil = claims.get("perfil", String.class);
                Long id = claims.get("id", Long.class);

                if (email != null && perfil != null && id != null) {
                    Long iat = claims.getIssuedAt() != null ? claims.getIssuedAt().getTime() : 0L;
                    
                    // Validação de Revogação de Token
                    Long versaoTokenAtiva = usuarioRepository.findVersaoTokenById(id).orElse(null);
                    
                    if (versaoTokenAtiva != null && iat >= (versaoTokenAtiva - 1000)) {
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
                        UsuarioPrincipal principal = new UsuarioPrincipal(id, email, iat);
                        
                        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                // Token inválido ou expirado — a requisição prossegue sem autenticação.
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
