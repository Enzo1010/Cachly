package br.com.questly.backend.questao;

import br.com.questly.backend.categoria.Categoria;
import br.com.questly.backend.categoria.CategoriaRepository;
import br.com.questly.backend.comum.erro.ConflitoDeDadosException;
import br.com.questly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.questly.backend.alternativa.Alternativa;
import br.com.questly.backend.alternativa.AlternativaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestaoService {

    private final QuestaoRepository questaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AlternativaRepository alternativaRepository;

    @Transactional
    public QuestaoResponse cadastrar(QuestaoRequest request) {
        Categoria categoria = buscarCategoriaAtiva(request.categoriaId());

        Questao questao = new Questao();
        aplicarDados(questao, request, categoria);

        return converterParaResponse(questaoRepository.save(questao));
    }

    public List<QuestaoResponse> listarAtivas() {
        return questaoRepository.findAllByAtivaTrueOrderByIdAsc()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public List<QuestaoEstudoResponse> listarParaEstudo(Long categoriaId, Integer limite) {
        if (limite == null || limite <= 0) {
            limite = 10;
        }
        PageRequest pageRequest = PageRequest.of(0, limite);
        List<Questao> questoes = categoriaId != null
                ? questaoRepository.findAllByCategoriaIdAndAtivaTrueOrderByIdAsc(categoriaId, pageRequest)
                : questaoRepository.findAllByAtivaTrueOrderByIdAsc(pageRequest);

        return questoes.stream().map(questao -> {
            List<Alternativa> alternativas = alternativaRepository
                    .findAllByQuestaoIdAndAtivaTrueOrderByOrdemAsc(questao.getId());
            
            List<AlternativaEstudoResponse> alternativasResponse = alternativas.stream()
                    .map(alt -> new AlternativaEstudoResponse(alt.getId(), alt.getTexto(), alt.getOrdem()))
                    .toList();
            
            return new QuestaoEstudoResponse(
                    questao.getId(),
                    questao.getEnunciado(),
                    questao.getDificuldade(),
                    questao.getXpBase(),
                    alternativasResponse
            );
        }).toList();
    }

    public QuestaoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public QuestaoResponse atualizar(Long id, QuestaoRequest request) {
        Questao questao = buscarEntidadePorId(id);
        Categoria categoria = buscarCategoriaAtiva(request.categoriaId());

        aplicarDados(questao, request, categoria);

        return converterParaResponse(questaoRepository.save(questao));
    }

    @Transactional
    public QuestaoResponse desativar(Long id) {
        Questao questao = buscarEntidadePorId(id);
        questao.setAtiva(false);

        return converterParaResponse(questaoRepository.save(questao));
    }

    private Questao buscarEntidadePorId(Long id) {
        return questaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Questão não encontrada com o ID: " + id
                ));
    }

    private Categoria buscarCategoriaAtiva(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada com o ID: " + categoriaId
                ));

        if (!Boolean.TRUE.equals(categoria.getAtiva())) {
            throw new ConflitoDeDadosException("A categoria informada está inativa");
        }

        return categoria;
    }

    private void aplicarDados(
            Questao questao,
            QuestaoRequest request,
            Categoria categoria
    ) {
        questao.setCategoria(categoria);
        questao.setEnunciado(request.enunciado().trim());
        questao.setExplicacao(request.explicacao().trim());
        questao.setDificuldade(request.dificuldade());
        questao.setXpBase(request.xpBase());
    }

    private QuestaoResponse converterParaResponse(Questao questao) {
        return new QuestaoResponse(
                questao.getId(),
                questao.getCategoria().getId(),
                questao.getCategoria().getNome(),
                questao.getEnunciado(),
                questao.getExplicacao(),
                questao.getDificuldade(),
                questao.getXpBase(),
                questao.getAtiva(),
                questao.getCriadoEm(),
                questao.getAtualizadoEm()
        );
    }
}
