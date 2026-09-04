package br.com.cachly.backend.resposta;

import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class GamificacaoEventListener {

    private final UsuarioRepository usuarioRepository;
    private final XpService xpService;

    @org.springframework.beans.factory.annotation.Value("${app.timezone:America/Sao_Paulo}")
    private String timezone;

    @EventListener
    @Transactional
    public void onQuestaoRespondida(QuestaoRespondidaEvent event) {
        int xpGanho = (event.correta() && event.primeiraVezCorreta()) ? xpService.calcularXpGanho(event.questao()) : 0;
        concederXpEAtualizarOfensiva(event.usuario().getId(), xpGanho);
    }

    @EventListener
    @Transactional
    public void onDesafioRespondido(br.com.cachly.backend.simulador.desafio.DesafioRespondidoEvent event) {
        int xpGanho = (event.correta() && event.primeiraVezCorreta()) ? event.xpRecompensa() : 0;
        concederXpEAtualizarOfensiva(event.usuario().getId(), xpGanho);
    }

    private void concederXpEAtualizarOfensiva(Long usuarioId, int xpGanho) {
        Usuario usuario = usuarioRepository.findByIdForUpdate(usuarioId).orElseThrow();

        if (xpGanho > 0) {
            usuario.setXpTotal(usuario.getXpTotal() + xpGanho);
            usuario.setXpSemanal(usuario.getXpSemanal() + xpGanho);
            usuario.setNivel(xpService.calcularNivel(usuario.getXpTotal()));
        }

        atualizarOfensiva(usuario);
        usuarioRepository.save(usuario);
    }

    private void atualizarOfensiva(Usuario usuario) {
        ZoneId zoneId = ZoneId.of(timezone);
        LocalDate hoje = LocalDate.now(zoneId);
        LocalDate ontem = hoje.minusDays(1);

        if (!hoje.equals(usuario.getDataUltimaOfensiva())) {
            if (ontem.equals(usuario.getDataUltimaOfensiva())) {
                usuario.setDiasOfensiva(usuario.getDiasOfensiva() + 1);
            } else {
                usuario.setDiasOfensiva(1);
            }
            usuario.setDataUltimaOfensiva(hoje);
        }
    }
}
