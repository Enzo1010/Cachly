package br.com.cachly.backend.usuario.aluno;

import br.com.cachly.backend.usuario.PerfilUsuario;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<RankingResponse> listarRanking(Pageable pageable) {
        Page<RankingProjection> usuarios = usuarioRepository.findRankingSemanal(PerfilUsuario.ALUNO, pageable);
        
        AtomicInteger posicaoAtual = new AtomicInteger((pageable.getPageNumber() * pageable.getPageSize()) + 1);
        
        return usuarios.map(projecao -> new RankingResponse(
                posicaoAtual.getAndIncrement(),
                projecao.getNome(),
                projecao.getNivel(),
                projecao.getXpSemanal(),
                projecao.getDiasOfensiva()
        ));
    }

    private final RankingSemanalHistoricoRepository historicoRepository;

    @Transactional(readOnly = true)
    public java.util.List<java.time.LocalDate> listarDatasHistorico() {
        return historicoRepository.findDatasDisponiveis();
    }

    @Transactional(readOnly = true)
    public Page<RankingHistoricoResponse> listarHistoricoPorData(java.time.LocalDate data, Pageable pageable) {
        return historicoRepository.findByDataSemana(data, pageable).map(historico -> new RankingHistoricoResponse(
                historico.getUsuario().getNome(),
                historico.getXpFinal(),
                historico.getPosicao(),
                historico.getDataSemana()
        ));
    }
}
