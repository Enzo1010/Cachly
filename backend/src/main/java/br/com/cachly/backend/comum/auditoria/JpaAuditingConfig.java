package br.com.cachly.backend.comum.auditoria;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Habilita o mecanismo de auditoria do Spring Data JPA.
 * O bean "auditorAwareImpl" é responsável por fornecer o ID do usuário
 * autenticado para os campos @CreatedBy e @LastModifiedBy.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl", dateTimeProviderRef = "offsetDateTimeProvider")
public class JpaAuditingConfig {

    @Bean
    public DateTimeProvider offsetDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
