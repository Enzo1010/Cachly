package br.com.cachly.backend.alternativa;

import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlternativaService {

    private final AlternativaRepository alternativaRepository;
    private final QuestaoRepository questaoRepository;

    @Transactional
    public AlternativaResponse cadastrar(Long questaoId, AlternativaRequest request) {
        Questao questao = buscarQuestaoPorId(questaoId);
        validarOrdemDuplicada(questaoId, request.ordem());
        validarAlternativaCorretaDuplicada(questaoId, request.correta());

        Alternativa alternativa = new Alternativa();
        alternativa.setQuestao(questao);
        aplicarDados(alternativa, request);

        return converterParaResponse(alternativaRepository.save(alternativa));
    }

    public List<AlternativaResponse> listarAtivas(Long questaoId) {
        buscarQuestaoPorId(questaoId);

        return alternativaRepository
                .findAllByQuestaoIdAndAtivaTrueOrderByOrdemAsc(questaoId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public AlternativaResponse buscarPorId(Long questaoId, Long id) {
        buscarQuestaoPorId(questaoId);
        return converterParaResponse(buscarEntidadePorId(questaoId, id));
    }

    @Transactional
    public AlternativaResponse atualizar(
            Long questaoId,
            Long id,
            AlternativaRequest request
    ) {
        buscarQuestaoPorId(questaoId);
        Alternativa alternativa = buscarEntidadePorId(questaoId, id);

        if (alternativaRepository.existsByQuestaoIdAndOrdemAndIdNot(
                questaoId,
                request.ordem(),
                id
        )) {
            throw new ConflitoDeDadosException(
                    "Já existe uma alternativa com essa ordem para a questão"
            );
        }

        if (Boolean.TRUE.equals(alternativa.getAtiva())
                && Boolean.TRUE.equals(request.correta())
                && alternativaRepository
                .existsByQuestaoIdAndCorretaTrueAndAtivaTrueAndIdNot(questaoId, id)) {
            throw new ConflitoDeDadosException(
                    "Já existe uma alternativa correta ativa para a questão"
            );
        }

        aplicarDados(alternativa, request);

        return converterParaResponse(alternativaRepository.save(alternativa));
    }

    @Transactional
    public AlternativaResponse desativar(Long questaoId, Long id) {
        buscarQuestaoPorId(questaoId);
        Alternativa alternativa = buscarEntidadePorId(questaoId, id);
        alternativa.setAtiva(false);

        return converterParaResponse(alternativaRepository.save(alternativa));
    }

    private Questao buscarQuestaoPorId(Long questaoId) {
        return questaoRepository.findById(questaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Questão não encontrada com o ID: " + questaoId
                ));
    }

    private Alternativa buscarEntidadePorId(Long questaoId, Long id) {
        return alternativaRepository.findByIdAndQuestaoId(id, questaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Alternativa não encontrada com o ID: " + id
                                + " para a questão: " + questaoId
                ));
    }

    private void validarOrdemDuplicada(Long questaoId, Short ordem) {
        if (alternativaRepository.existsByQuestaoIdAndOrdem(questaoId, ordem)) {
            throw new ConflitoDeDadosException(
                    "Já existe uma alternativa com essa ordem para a questão"
            );
        }
    }

    private void validarAlternativaCorretaDuplicada(Long questaoId, Boolean correta) {
        if (Boolean.TRUE.equals(correta)
                && alternativaRepository
                .existsByQuestaoIdAndCorretaTrueAndAtivaTrue(questaoId)) {
            throw new ConflitoDeDadosException(
                    "Já existe uma alternativa correta ativa para a questão"
            );
        }
    }

    private void aplicarDados(Alternativa alternativa, AlternativaRequest request) {
        alternativa.setTexto(request.texto().trim());
        alternativa.setCorreta(request.correta());
        alternativa.setOrdem(request.ordem());
    }

    private AlternativaResponse converterParaResponse(Alternativa alternativa) {
        return new AlternativaResponse(
                alternativa.getId(),
                alternativa.getQuestao().getId(),
                alternativa.getTexto(),
                alternativa.getCorreta(),
                alternativa.getOrdem(),
                alternativa.getAtiva(),
                alternativa.getCriadoEm(),
                alternativa.getAtualizadoEm(),
                alternativa.getCriadoPor(),
                alternativa.getAtualizadoPor()
        );
    }
}
