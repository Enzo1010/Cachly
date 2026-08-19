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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

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

        Usuario usuario = usuarioRepository.findByIdForUpdate(usuarioAutenticado.getId())
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
        }

        TentativaQuestao tentativa = new TentativaQuestao();
        tentativa.setUsuario(usuario);
        tentativa.setQuestao(questao);
        tentativa.setAlternativa(alternativa);
        tentativa.setCorreta(correta);
        tentativa.setXpConcedido(xpGanho);

        atualizarOfensivaSeNecessario(usuario);

        // Se o usuário foi atualizado (XP ou ofensiva), salva.
        // Já estávamos salvando dentro do if (correta), mas agora a ofensiva também pode modificar o usuário,
        // então é mais seguro dar um save() aqui garantidamente.
        usuarioRepository.save(usuario);

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

    private void atualizarOfensivaSeNecessario(Usuario usuario) {
        LocalDate hoje = LocalDate.now();
        OffsetDateTime inicioDoDia = hoje.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime fimDoDia = hoje.atTime(23, 59, 59, 999999999).atZone(ZoneId.systemDefault()).toOffsetDateTime();

        long respostasHoje = tentativaQuestaoRepository.countByUsuarioIdAndRespondidaEmBetween(
                usuario.getId(), inicioDoDia, fimDoDia
        );

        // A streak só aumenta se esta for exatamente a 2ª questão do dia.
        // Se for a 1ª (respostasHoje == 0), ainda não bateu a meta.
        // Se for a 3ª ou mais (respostasHoje >= 2), a ofensiva já foi calculada.
        if (respostasHoje == 1) {
            LocalDate dataUltima = usuario.getDataUltimaOfensiva();

            if (dataUltima != null && dataUltima.equals(hoje.minusDays(1))) {
                // Ontem o aluno também completou a meta, estende a ofensiva!
                usuario.setDiasOfensiva(usuario.getDiasOfensiva() + 1);
            } else if (dataUltima == null || dataUltima.isBefore(hoje.minusDays(1))) {
                // Perdeu a ofensiva ou é a primeira vez
                usuario.setDiasOfensiva(1);
            }
            
            // Registra que hoje ele completou a meta
            usuario.setDataUltimaOfensiva(hoje);
        }
    }
}
