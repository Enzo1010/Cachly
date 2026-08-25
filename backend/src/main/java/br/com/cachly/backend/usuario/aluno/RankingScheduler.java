package br.com.cachly.backend.usuario.aluno;

import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private static final Logger log = LoggerFactory.getLogger(RankingScheduler.class);
    private final UsuarioRepository usuarioRepository;
    private final RankingSemanalHistoricoRepository historicoRepository;

    @Value("${ranking.historico.top-n:10}")
    private int topN;

    @Value("${app.timezone:America/Sao_Paulo}")
    private String timezone;

    @Scheduled(cron = "0 0 0 * * MON", zone = "${app.timezone:America/Sao_Paulo}")
    @Transactional
    public void zerarXpSemanal() {
        log.info("Iniciando rotina semanal de zeramento do XP Semanal para o Ranking...");
        
        var rankingAtual = usuarioRepository.findRankingSemanal(
            PerfilUsuario.ALUNO, 
            PageRequest.of(0, topN)
        ).getContent();
        
        if (!rankingAtual.isEmpty()) {
            LocalDate hoje = LocalDate.now(ZoneId.of(timezone));
            AtomicInteger posicao = new AtomicInteger(1);
            
            List<RankingSemanalHistorico> historicos = rankingAtual.stream()
                .map(r -> {
                    Usuario u = usuarioRepository.getReferenceById(r.getId());
                    return new RankingSemanalHistorico(u, r.getXpSemanal(), posicao.getAndIncrement(), hoje);
                })
                .toList();
                
            historicoRepository.saveAll(historicos);
            log.info("Salvo o snapshot do Top {} no historico de rankings.", topN);
        }

        usuarioRepository.resetarXpSemanal();
        log.info("Rotina de zeramento do XP Semanal finalizada com sucesso.");
    }
}
