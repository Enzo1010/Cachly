package br.com.questly.backend.usuario.aluno;

import br.com.questly.backend.comum.erro.ConflitoDeDadosException;
import br.com.questly.backend.resposta.DesempenhoCategoriaProjection;
import br.com.questly.backend.usuario.Usuario;
import br.com.questly.backend.resposta.TentativaQuestaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlunoDesempenhoService {

    private final TentativaQuestaoRepository tentativaQuestaoRepository;

    public Page<HistoricoTentativaResponse> obterHistorico(Usuario usuario, Pageable pageable) {
        validarUsuario(usuario);

        return tentativaQuestaoRepository
                .findByUsuarioIdOrderByRespondidaEmDesc(usuario.getId(), pageable)
                .map(tentativa -> new HistoricoTentativaResponse(
                        tentativa.getId(),
                        tentativa.getQuestao().getId(),
                        tentativa.getQuestao().getEnunciado(),
                        tentativa.getAlternativa().getId(),
                        tentativa.getAlternativa().getTexto(),
                        tentativa.getCorreta(),
                        tentativa.getXpConcedido(),
                        tentativa.getRespondidaEm()
                ));
    }

    public DesempenhoResponse obterEstatisticas(Usuario usuario) {
        validarUsuario(usuario);

        long totalTentativas = tentativaQuestaoRepository.countByUsuarioId(usuario.getId());
        long acertosTotais = tentativaQuestaoRepository.countByUsuarioIdAndCorretaTrue(usuario.getId());
        int taxaAcertoGeral = calcularPorcentagem(totalTentativas, acertosTotais);

        List<DesempenhoCategoriaProjection> projections = tentativaQuestaoRepository
                .findEstatisticasPorCategoria(usuario.getId());

        List<DesempenhoCategoriaResponse> estatisticasPorCategoria = projections.stream()
                .map(proj -> {
                    long tentativas = proj.getTotalTentativas() != null ? proj.getTotalTentativas() : 0L;
                    long acertos = proj.getAcertos() != null ? proj.getAcertos() : 0L;
                    return new DesempenhoCategoriaResponse(
                            proj.getCategoriaId(),
                            proj.getCategoriaNome(),
                            tentativas,
                            acertos,
                            calcularPorcentagem(tentativas, acertos)
                    );
                })
                .toList();

        return new DesempenhoResponse(totalTentativas, acertosTotais, taxaAcertoGeral, estatisticasPorCategoria);
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new ConflitoDeDadosException("Usuário não autenticado");
        }
    }

    private int calcularPorcentagem(long total, long parte) {
        if (total == 0) {
            return 0;
        }
        return (int) Math.round((double) parte / total * 100);
    }
}
