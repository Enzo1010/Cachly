package br.com.cachly.backend.resposta;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.alternativa.AlternativaRepository;
import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RespostaService {

    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TentativaQuestaoRepository tentativaQuestaoRepository;
    private final XpService xpService;

    @Transactional
    public RespostaResponse responder(Long questaoId, RespostaRequest request, Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null || usuarioAutenticado.getId() == null) {
            throw new ConflitoDeDadosException("Usuário não autenticado");
        }

        Usuario usuario = usuarioRepository.findById(usuarioAutenticado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new ConflitoDeDadosException("Usuário inativo não pode responder questões");
        }

        Questao questao = questaoRepository.findByIdAndAtivaTrue(questaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Questão não encontrada ou inativa com o ID: " + questaoId
                ));

        Alternativa alternativa = alternativaRepository.findByIdAndQuestaoIdAndAtivaTrue(
                request.alternativaId(),
                questaoId
        ).orElseThrow(() -> new RecursoNaoEncontradoException(
                "Alternativa não encontrada, inativa ou não pertence à questão informada"
        ));

        boolean correta = Boolean.TRUE.equals(alternativa.getCorreta());

        int xpGanho = 0;
        if (correta) {
            xpGanho = xpService.calcularXpGanho(questao);
            usuario.setXpTotal(usuario.getXpTotal() + xpGanho);
            usuario.setNivel(xpService.calcularNivel(usuario.getXpTotal()));
            usuarioRepository.save(usuario);
        }

        TentativaQuestao tentativa = new TentativaQuestao();
        tentativa.setUsuario(usuario);
        tentativa.setQuestao(questao);
        tentativa.setAlternativa(alternativa);
        tentativa.setCorreta(correta);
        tentativa.setXpConcedido(xpGanho);

        TentativaQuestao salva = tentativaQuestaoRepository.save(tentativa);

        return new RespostaResponse(
                salva.getId(),
                correta,
                questao.getExplicacao(),
                xpGanho,
                usuario.getNivel(),
                xpService.nomeDoNivel(usuario.getNivel()),
                usuario.getXpTotal()
        );
    }
}
