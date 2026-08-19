package br.com.cachly.backend.comum.auditoria;

import br.com.cachly.backend.usuario.Usuario;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementação de AuditorAware que extrai o ID do usuário autenticado
 * a partir do SecurityContext populado pelo SecurityFilter (JWT).
 *
 * Retorna Optional.empty() quando não há autenticação (endpoints públicos, testes),
 * fazendo com que os campos @CreatedBy/@LastModifiedBy fiquem null nesses cenários.
 */
@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Usuario usuario) {
            return Optional.of(usuario.getId());
        }

        return Optional.empty();
    }
}
