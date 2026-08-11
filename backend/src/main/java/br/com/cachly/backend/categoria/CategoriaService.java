package br.com.cachly.backend.categoria;

import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaResponse cadastrar(CategoriaRequest request) {
        String nome = request.nome().trim();
        validarNomeDuplicado(nome);

        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao(normalizarDescricao(request.descricao()));

        return converterParaResponse(categoriaRepository.save(categoria));
    }

    public List<CategoriaResponse> listarAtivas() {
        return categoriaRepository.findAllByAtivaTrueOrderByNomeAsc()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public CategoriaResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntidadePorId(id);
        String nome = request.nome().trim();

        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoDeDadosException("Já existe uma categoria com esse nome");
        }

        categoria.setNome(nome);
        categoria.setDescricao(normalizarDescricao(request.descricao()));

        return converterParaResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse desativar(Long id) {
        Categoria categoria = buscarEntidadePorId(id);
        categoria.setAtiva(false);

        return converterParaResponse(categoriaRepository.save(categoria));
    }

    private Categoria buscarEntidadePorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada com o ID: " + id
                ));
    }

    private void validarNomeDuplicado(String nome) {
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoDeDadosException("Já existe uma categoria com esse nome");
        }
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }

        return descricao.trim();
    }

    private CategoriaResponse converterParaResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getAtiva(),
                categoria.getCriadoEm(),
                categoria.getAtualizadoEm()
        );
    }
}
