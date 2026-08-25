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

    @Transactional
    public RespostaResponse responder(Long questaoId, RespostaRequest request, Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new ConflitoDeDadosException("Usuário inativo não pode responder questões");
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
                    usuario.getId(), questao.getId()
            );
            if (!jaAcertouAntes) {
                primeiraVezCorreta = true;
                xpGanho = xpService.calcularXpGanho(questao);
            }
        }

        TentativaQuestao tentativa = new TentativaQuestao();
        tentativa.setUsuario(usuario);
        tentativa.setQuestao(questao);
        tentativa.setAlternativa(alternativa);
        tentativa.setCorreta(correta);
        tentativa.setXpConcedido(xpGanho);

        TentativaQuestao salva = tentativaQuestaoRepository.save(tentativa);

        eventPublisher.publishEvent(new QuestaoRespondidaEvent(usuario, questao, correta, primeiraVezCorreta));

        return new RespostaResponse(
                salva.getId(),
                correta,
                questao.getExplicacao(),
                xpGanho,
                // O nível e xp total atuais que serão retornados podem estar defasados pois o evento pode 
                // rodar antes do flush ou depois, mas como é sincrono e transacional, ele atualiza a mesma 
                // instância gerenciada pelo Hibernate caso esteja no mesmo escopo. 
                usuario.getNivel(),
                xpService.nomeDoNivel(usuario.getNivel()),
                usuario.getXpTotal()
        );
    }
}
