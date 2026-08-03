package br.com.questly.backend.service;

import br.com.questly.backend.dto.QuestaoRequest;
import br.com.questly.backend.dto.QuestaoResponse;
import br.com.questly.backend.entity.Categoria;
import br.com.questly.backend.entity.Questao;
import br.com.questly.backend.exception.ConflitoDeDadosException;
import br.com.questly.backend.exception.RecursoNaoEncontradoException;
import br.com.questly.backend.repository.CategoriaRepository;
import br.com.questly.backend.repository.QuestaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
