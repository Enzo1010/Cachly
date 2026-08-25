package br.com.cachly.backend.usuario.aluno;

import br.com.cachly.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResetRankingSemanalJob {

    private final JdbcTemplate jdbcTemplate;

    // Executa toda segunda-feira à meia-noite (UTC)
    @Scheduled(cron = "0 0 0 * * MON", zone = "UTC")
    @Transactional
    public void resetarXpSemanal() {
        log.info("Iniciando reset do XP Semanal de todos os usuários...");
        int atualizados = jdbcTemplate.update("UPDATE usuarios SET xp_semanal = 0 WHERE xp_semanal > 0");
        log.info("Reset concluído. {} usuários tiveram o XP semanal zerado.", atualizados);
    }
}
