package br.com.cachly.backend.resposta;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import br.com.cachly.backend.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RespostaService {

    private final QuestaoRepository questaoRepository;
    private final TentativaQuestaoRepository tentativaQuestaoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final XpService xpService;
    private final br.com.cachly.backend.usuario.UsuarioRepository usuarioRepository;

    @Transactional
    public RespostaResponse responder(Long questaoId, RespostaRequest request, Usuario usuario) {
        Usuario usuarioBloqueado = usuarioRepository.findByIdForUpdate(usuario.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (!Boolean.TRUE.equals(usuarioBloqueado.getAtivo())) {
            throw new br.com.cachly.backend.comum.erro.RegraNegocioException("Usuário inativo não pode responder questões");
        }

        Questao questao = questaoRepository.findByIdAndAtivaTrue(questaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Questão não encontrada ou inativa com o ID: " + questaoId
                ));

        Alternativa alternativa = questao.getAlternativas().stream()
                .filter(a -> a.getId().equals(request.alternativaId()) && Boolean.TRUE.equals(a.getAtiva()))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Alternativa não encontrada, inativa ou não pertence à questão informada"
                ));

        boolean correta = Boolean.TRUE.equals(alternativa.getCorreta());
        boolean primeiraVezCorreta = false;
        int xpGanho = 0;

        if (correta) {
            boolean jaAcertouAntes = tentativaQuestaoRepository.existsByUsuarioIdAndQuestaoIdAndCorretaTrue(
                    usuarioBloqueado.getId(), questao.getId()
            );
            if (!jaAcertouAntes) {
                primeiraVezCorreta = true;
                xpGanho = xpService.calcularXpGanho(questao);
            }
        }

        TentativaQuestao tentativa = new TentativaQuestao();
        tentativa.setUsuario(usuarioBloqueado);
        tentativa.setQuestao(questao);
        tentativa.setAlternativa(alternativa);
        tentativa.setCorreta(correta);
        tentativa.setXpConcedido(xpGanho);

        TentativaQuestao salva = tentativaQuestaoRepository.save(tentativa);

        eventPublisher.publishEvent(new QuestaoRespondidaEvent(usuarioBloqueado, questao, correta, primeiraVezCorreta));

        Long alternativaCorretaId = questao.getAlternativas().stream()
                .filter(a -> Boolean.TRUE.equals(a.getCorreta()))
                .map(Alternativa::getId)
                .findFirst()
                .orElse(null);

        // O evento modificou o usuário em uma instância persistente gerenciada pelo listener.
        // Recarregamos a instância para garantir que vamos retornar os dados frescos (XP/Nível atualizados)
        // e não os dados "stale" do objeto desanexado original.
        Usuario usuarioAtualizado = usuarioRepository.findById(usuarioBloqueado.getId()).orElse(usuarioBloqueado);

        return new RespostaResponse(
                salva.getId(),
                correta,
                alternativaCorretaId,
                questao.getExplicacao(),
                xpGanho,
                usuarioAtualizado.getNivel(),
                xpService.nomeDoNivel(usuarioAtualizado.getNivel()),
                usuarioAtualizado.getXpTotal()
        );
    }
}
