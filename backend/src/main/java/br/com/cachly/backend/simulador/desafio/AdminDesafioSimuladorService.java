package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDesafioSimuladorService {

    private final DesafioSimuladorRepository repository;

    @Transactional(readOnly = true)
    public List<DesafioCacheResponse> listarTodos() {
        return repository.findAll().stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DesafioCacheResponse obterPorId(String id) {
        return repository.findById(id)
                .map(this::converterParaResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desafio não encontrado"));
    }

    @Transactional
    public DesafioCacheResponse cadastrar(DesafioSimuladorRequest request) {
        if (repository.existsById(request.id())) {
            throw new ConflitoDeDadosException("Já existe um desafio com este identificador (ID)");
        }
        DesafioSimulador entidade = new DesafioSimulador();
        entidade.setId(request.id());
        atualizarEntidade(entidade, request);
        return converterParaResponse(repository.save(entidade));
    }

    @Transactional
    public DesafioCacheResponse atualizar(String id, DesafioSimuladorRequest request) {
        DesafioSimulador entidade = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desafio não encontrado"));
        
        atualizarEntidade(entidade, request);
        return converterParaResponse(repository.save(entidade));
    }

    @Transactional
    public void excluir(String id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Desafio não encontrado");
        }
        repository.deleteById(id);
    }

    private void atualizarEntidade(DesafioSimulador entidade, DesafioSimuladorRequest request) {
        entidade.setTitulo(request.titulo());
        entidade.setDescricao(request.descricao());
        entidade.setDificuldade(request.dificuldade());
        entidade.setXpRecompensa(request.xpRecompensa());
        entidade.setConfiguracao(request.configuracao());
        entidade.setPergunta(request.pergunta());
        entidade.setOpcaoCorretaId(request.opcaoCorretaId());
        entidade.setDica(request.dica());
        entidade.setExplicacao(request.explicacao());
        
        entidade.getOpcoes().clear();
        if (request.opcoes() != null) {
            request.opcoes().forEach(o -> 
                entidade.getOpcoes().add(new OpcaoDesafioEmbeddable(o.id(), o.texto()))
            );
        }
    }

    private DesafioCacheResponse converterParaResponse(DesafioSimulador entidade) {
        List<OpcaoDesafioResponse> opcoes = entidade.getOpcoes().stream()
                .map(o -> new OpcaoDesafioResponse(o.getId(), o.getTexto()))
                .toList();

        return new DesafioCacheResponse(
                entidade.getId(),
                entidade.getTitulo(),
                entidade.getDescricao(),
                entidade.getDificuldade(),
                entidade.getXpRecompensa(),
                entidade.getConfiguracao(),
                entidade.getPergunta(),
                opcoes,
                entidade.getDica()
        );
    }
}
