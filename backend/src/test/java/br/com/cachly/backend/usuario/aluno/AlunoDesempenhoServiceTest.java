package br.com.cachly.backend.usuario.aluno;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.usuario.Usuario;
import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.resposta.DesempenhoCategoriaProjection;
import br.com.cachly.backend.resposta.TentativaQuestao;
import br.com.cachly.backend.resposta.TentativaQuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlunoDesempenhoServiceTest {

    @Mock
    private TentativaQuestaoRepository tentativaQuestaoRepository;

    @InjectMocks
    private AlunoDesempenhoService alunoDesempenhoService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(10L);
    }

    @Test
    void deveRetornarHistoricoPaginado() {
        Questao questao = new Questao();
        questao.setId(1L);
        questao.setEnunciado("O que é um bit?");

        Alternativa alternativa = new Alternativa();
        alternativa.setId(100L);
        alternativa.setTexto("0 ou 1");

        TentativaQuestao tentativa = new TentativaQuestao();
        tentativa.setId(500L);
        tentativa.setQuestao(questao);
        tentativa.setAlternativa(alternativa);
        tentativa.setCorreta(true);
        tentativa.setXpConcedido(10);
        tentativa.setRespondidaEm(OffsetDateTime.now());

        PageRequest pageable = PageRequest.of(0, 10);
        when(tentativaQuestaoRepository.findByUsuarioIdOrderByRespondidaEmDesc(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(tentativa)));

        Page<HistoricoTentativaResponse> historico = alunoDesempenhoService.obterHistorico(usuario, pageable);

        assertNotNull(historico);
        assertEquals(1, historico.getTotalElements());
        HistoricoTentativaResponse response = historico.getContent().get(0);
        assertEquals(500L, response.id());
        assertEquals(1L, response.questaoId());
        assertEquals("O que é um bit?", response.questaoEnunciado());
        assertEquals(100L, response.alternativaId());
        assertEquals("0 ou 1", response.alternativaTexto());
        assertEquals(true, response.correta());
        assertEquals(10, response.xpConcedido());
    }

    @Test
    void deveRetornarEstatisticasGeraisEPorCategoria() {
        when(tentativaQuestaoRepository.countByUsuarioId(10L)).thenReturn(10L);
        when(tentativaQuestaoRepository.countByUsuarioIdAndCorretaTrue(10L)).thenReturn(8L);

        DesempenhoCategoriaProjection proj = new DesempenhoCategoriaProjection() {
            @Override
            public Long getCategoriaId() { return 1L; }
            @Override
            public String getCategoriaNome() { return "Hardware"; }
            @Override
            public Long getTotalTentativas() { return 4L; }
            @Override
            public Long getAcertos() { return 3L; }
        };

        when(tentativaQuestaoRepository.findEstatisticasPorCategoria(10L))
                .thenReturn(List.of(proj));

        DesempenhoResponse estatisticas = alunoDesempenhoService.obterEstatisticas(usuario);

        assertEquals(10L, estatisticas.totalTentativas());
        assertEquals(8L, estatisticas.acertos());
        assertEquals(80, estatisticas.taxaAcerto());

        assertEquals(1, estatisticas.estatisticasPorCategoria().size());
        DesempenhoCategoriaResponse catStats = estatisticas.estatisticasPorCategoria().get(0);
        assertEquals(1L, catStats.categoriaId());
        assertEquals("Hardware", catStats.categoriaNome());
        assertEquals(4L, catStats.totalTentativas());
        assertEquals(3L, catStats.acertos());
        assertEquals(75, catStats.taxaAcerto());
    }

    @Test
    void deveLidarComZeroTentativasSemErroDeDivisaoPorZero() {
        when(tentativaQuestaoRepository.countByUsuarioId(10L)).thenReturn(0L);
        when(tentativaQuestaoRepository.countByUsuarioIdAndCorretaTrue(10L)).thenReturn(0L);
        when(tentativaQuestaoRepository.findEstatisticasPorCategoria(10L)).thenReturn(List.of());

        DesempenhoResponse estatisticas = alunoDesempenhoService.obterEstatisticas(usuario);

        assertEquals(0L, estatisticas.totalTentativas());
        assertEquals(0L, estatisticas.acertos());
        assertEquals(0, estatisticas.taxaAcerto());
        assertEquals(0, estatisticas.estatisticasPorCategoria().size());
    }

    @Test
    void deveLancarExcecaoSeUsuarioInvalido() {
        assertThrows(ConflitoDeDadosException.class, () -> alunoDesempenhoService.obterEstatisticas(null));
        assertThrows(ConflitoDeDadosException.class, () -> alunoDesempenhoService.obterHistorico(new Usuario(), PageRequest.of(0, 10)));
    }
}
