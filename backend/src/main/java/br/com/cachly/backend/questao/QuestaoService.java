package br.com.cachly.backend.questao;

import br.com.cachly.backend.categoria.Categoria;
import br.com.cachly.backend.categoria.CategoriaRepository;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.alternativa.Alternativa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import br.com.cachly.backend.alternativa.AlternativaRequest;
import br.com.cachly.backend.alternativa.AlternativaResponse;
import br.com.cachly.backend.comum.erro.RegraNegocioException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestaoService {

    private final QuestaoRepository questaoRepository;
    private final CategoriaRepository categoriaRepository;

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

    public List<QuestaoResponse> listarTodas() {
        return questaoRepository.findAll()
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
            List<AlternativaEstudoResponse> alternativasResponse = questao.getAlternativas().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getAtiva()))
                    .sorted(java.util.Comparator.comparing(Alternativa::getOrdem))
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

    @Transactional
    public QuestaoResponse ativar(Long id) {
        Questao questao = buscarEntidadePorId(id);
        questao.setAtiva(true);

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
            throw new RegraNegocioException("A categoria informada está inativa");
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

        long corretas = request.alternativas().stream().filter(AlternativaRequest::correta).count();
        if (corretas != 1) {
            throw new RegraNegocioException("A questão deve ter exatamente uma alternativa correta");
        }

        List<Long> idsRecebidos = request.alternativas().stream()
                .map(AlternativaRequest::id)
                .filter(java.util.Objects::nonNull)
                .toList();

        // 1. Desativar alternativas que não vieram no request (soft delete)
        questao.getAlternativas().stream()
                .filter(a -> !idsRecebidos.contains(a.getId()))
                .forEach(a -> a.setAtiva(false));

        // 2. Atualizar existentes e adicionar novas
        request.alternativas().forEach(altReq -> {
            if (altReq.id() != null) {
                questao.getAlternativas().stream()
                        .filter(a -> altReq.id().equals(a.getId()))
                        .findFirst()
                        .ifPresent(alt -> {
                            alt.setTexto(altReq.texto().trim());
                            alt.setCorreta(altReq.correta());
                            alt.setOrdem(altReq.ordem());
                            alt.setAtiva(true);
                        });
            } else {
                Alternativa novaAlt = new Alternativa();
                novaAlt.setTexto(altReq.texto().trim());
                novaAlt.setCorreta(altReq.correta());
                novaAlt.setOrdem(altReq.ordem());
                novaAlt.setAtiva(true);
                novaAlt.setQuestao(questao);
                questao.getAlternativas().add(novaAlt);
            }
        });
    }

    private QuestaoResponse converterParaResponse(Questao questao) {
        List<AlternativaResponse> alternativasResp = questao.getAlternativas().stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtiva()))
                .map(a -> new AlternativaResponse(
                        a.getId(),
                        a.getQuestao().getId(),
                        a.getTexto(),
                        a.getCorreta(),
                        a.getOrdem(),
                        a.getAtiva(),
                        a.getCriadoEm(),
                        a.getAtualizadoEm(),
                        a.getCriadoPor(),
                        a.getAtualizadoPor()
                )).toList();

        return new QuestaoResponse(
                questao.getId(),
                questao.getCategoria().getId(),
                questao.getCategoria().getNome(),
                questao.getEnunciado(),
                questao.getExplicacao(),
                questao.getDificuldade(),
                questao.getXpBase(),
                questao.getAtiva(),
                alternativasResp,
                questao.getCriadoEm(),
                questao.getAtualizadoEm(),
                questao.getCriadoPor(),
                questao.getAtualizadoPor()
        );
    }
}
