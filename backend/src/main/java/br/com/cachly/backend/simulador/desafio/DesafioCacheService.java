package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.resposta.XpService;
import br.com.cachly.backend.simulador.PoliticaSubstituicao;
import br.com.cachly.backend.simulador.SimulacaoRequest;
import br.com.cachly.backend.simulador.SimulacaoResponse;
import br.com.cachly.backend.simulador.SimuladorCacheService;
import br.com.cachly.backend.simulador.TipoMapeamento;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.cachly.backend.comum.erro.RegraNegocioException;

@Service
@RequiredArgsConstructor
public class DesafioCacheService {

    private final SimuladorCacheService simuladorCacheService;
    private final UsuarioRepository usuarioRepository;
    private final XpService xpService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final DesafioConcluidoRepository desafioConcluidoRepository;
    private final DesafioSimuladorRepository desafioRepository;

    public List<DesafioCacheResponse> listarDesafios() {
        return desafioRepository.findAll().stream()
                .map(this::paraResponse)
                .toList();
    }

    public DesafioCacheResponse obterDesafio(String id) {
        DesafioSimulador desafio = buscarDesafioOuFalhar(id);
        return paraResponse(desafio);
    }

    @Transactional
    public ResultadoDesafioResponse verificarDesafio(String id, VerificarDesafioRequest request, Usuario usuario) {
        DesafioSimulador desafio = buscarDesafioOuFalhar(id);

        Usuario usuarioBloqueado = null;
        if (usuario != null) {
            usuarioBloqueado = usuarioRepository.findByIdForUpdate(usuario.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
            if (!Boolean.TRUE.equals(usuarioBloqueado.getAtivo())) {
                throw new RegraNegocioException("Usuário inativo não pode responder desafios");
            }
        }

        boolean correto = desafio.getOpcaoCorretaId().equalsIgnoreCase(request.opcaoSelecionadaId().trim());
        int xpGanho = 0;

        if (correto && usuarioBloqueado != null) {
            boolean jaConcluido = desafioConcluidoRepository.existsByUsuarioIdAndDesafioId(usuarioBloqueado.getId(), id);
            if (!jaConcluido) {
                DesafioConcluido concluido = new DesafioConcluido();
                concluido.setUsuario(usuarioBloqueado);
                concluido.setDesafioId(id);
                desafioConcluidoRepository.save(concluido);

                xpGanho = desafio.getXpRecompensa();
                eventPublisher.publishEvent(new DesafioRespondidoEvent(usuarioBloqueado, id, xpGanho, true, true));
            } else {
                eventPublisher.publishEvent(new DesafioRespondidoEvent(usuarioBloqueado, id, 0, true, false));
            }
        } else if (!correto && usuarioBloqueado != null) {
            eventPublisher.publishEvent(new DesafioRespondidoEvent(usuarioBloqueado, id, 0, false, false));
        }

        SimulacaoResponse simulacao = simuladorCacheService.executarSimulacao(desafio.getConfiguracao());

        Usuario usuarioAtualizado = usuarioBloqueado != null ? usuarioRepository.findById(usuarioBloqueado.getId()).orElse(usuarioBloqueado) : null;
        Integer nivelAtual = (usuarioAtualizado != null && usuarioAtualizado.getNivel() != null) ? usuarioAtualizado.getNivel() : 1;
        String nomeNivel = xpService.nomeDoNivel(nivelAtual);
        Integer xpTotalAtual = (usuarioAtualizado != null && usuarioAtualizado.getXpTotal() != null) ? usuarioAtualizado.getXpTotal() : 0;

        return new ResultadoDesafioResponse(
                correto,
                desafio.getOpcaoCorretaId(),
                correto ? desafio.getExplicacao() : "Não foi dessa vez! Dica: " + desafio.getDica(),
                xpGanho,
                simulacao,
                nivelAtual,
                nomeNivel,
                xpTotalAtual
        );
    }

    private DesafioSimulador buscarDesafioOuFalhar(String id) {
        return desafioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desafio não encontrado com o identificador: " + id));
    }

    private DesafioCacheResponse paraResponse(DesafioSimulador desafio) {
        List<OpcaoDesafioResponse> opcoes = desafio.getOpcoes().stream()
                .map(o -> new OpcaoDesafioResponse(o.getId(), o.getTexto()))
                .toList();

        return new DesafioCacheResponse(
                desafio.getId(),
                desafio.getTitulo(),
                desafio.getDescricao(),
                desafio.getDificuldade(),
                desafio.getXpRecompensa(),
                desafio.getConfiguracao(),
                desafio.getPergunta(),
                opcoes,
                desafio.getDica()
        );
    }
}
