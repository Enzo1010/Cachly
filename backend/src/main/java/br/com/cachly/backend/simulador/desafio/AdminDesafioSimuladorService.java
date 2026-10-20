package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.comum.erro.RegraNegocioException;
import br.com.cachly.backend.simulador.SimuladorCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AdminDesafioSimuladorService {

    private final DesafioSimuladorRepository repository;
    private final SimuladorCacheService simuladorCacheService;

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
        validarConsistencia(request);

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

    private void validarConsistencia(DesafioSimuladorRequest request) {
        Set<String> idsNormalizados = new HashSet<>();
        for (OpcaoDesafioRequest opcao : request.opcoes()) {
            String idNormalizado = opcao.id().trim().toUpperCase(Locale.ROOT);
            if (!idsNormalizados.add(idNormalizado)) {
                throw new RegraNegocioException("As opções do desafio devem possuir identificadores únicos");
            }
        }

        String opcaoCorretaNormalizada = request.opcaoCorretaId().trim().toUpperCase(Locale.ROOT);
        if (!idsNormalizados.contains(opcaoCorretaNormalizada)) {
            throw new RegraNegocioException("A opção correta deve corresponder a uma das opções do desafio");
        }

        // Reutiliza as regras de domínio do simulador antes de persistir a configuração.
        simuladorCacheService.executarSimulacao(request.configuracao());
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
