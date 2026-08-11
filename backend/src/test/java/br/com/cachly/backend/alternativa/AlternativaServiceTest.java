package br.com.cachly.backend.alternativa;

import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import br.com.cachly.backend.questao.Questao;
import br.com.cachly.backend.questao.QuestaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlternativaServiceTest {

    @Mock
    private AlternativaRepository alternativaRepository;

    @Mock
    private QuestaoRepository questaoRepository;

    @InjectMocks
    private AlternativaService alternativaService;

    @Test
    void deveCadastrarAlternativaQuandoDadosForemValidos() {
        Questao questao = criarQuestao(1L);
        AlternativaRequest request = criarRequest("  Resposta correta  ", true, (short) 1);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.save(any(Alternativa.class)))
                .thenAnswer(invocacao -> {
                    Alternativa alternativa = invocacao.getArgument(0);
                    OffsetDateTime agora = OffsetDateTime.now();
                    alternativa.setId(10L);
                    alternativa.setCriadoEm(agora);
                    alternativa.setAtualizadoEm(agora);
                    return alternativa;
                });

        AlternativaResponse response = alternativaService.cadastrar(1L, request);

        ArgumentCaptor<Alternativa> captor = ArgumentCaptor.forClass(Alternativa.class);
        verify(alternativaRepository).save(captor.capture());

        assertEquals(10L, response.id());
        assertEquals(1L, response.questaoId());
        assertEquals("Resposta correta", response.texto());
        assertEquals(true, response.correta());
        assertEquals((short) 1, response.ordem());
        assertEquals(questao, captor.getValue().getQuestao());
    }

    @Test
    void deveRecusarCadastroQuandoQuestaoNaoExistir() {
        AlternativaRequest request = criarRequest("Alternativa", false, (short) 1);
        when(questaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> alternativaService.cadastrar(99L, request)
        );

        verify(alternativaRepository, never()).save(any(Alternativa.class));
    }

    @Test
    void deveRecusarCadastroQuandoOrdemJaExistirNaQuestao() {
        Questao questao = criarQuestao(1L);
        AlternativaRequest request = criarRequest("Alternativa", false, (short) 2);
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.existsByQuestaoIdAndOrdem(1L, (short) 2))
                .thenReturn(true);

        assertThrows(
                ConflitoDeDadosException.class,
                () -> alternativaService.cadastrar(1L, request)
        );

        verify(alternativaRepository, never()).save(any(Alternativa.class));
    }

    @Test
    void deveRecusarCadastroQuandoJaExistirAlternativaCorretaAtiva() {
        Questao questao = criarQuestao(1L);
        AlternativaRequest request = criarRequest("Outra correta", true, (short) 2);
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository
                .existsByQuestaoIdAndCorretaTrueAndAtivaTrue(1L))
                .thenReturn(true);

        assertThrows(
                ConflitoDeDadosException.class,
                () -> alternativaService.cadastrar(1L, request)
        );

        verify(alternativaRepository, never()).save(any(Alternativa.class));
    }

    @Test
    void deveListarAlternativasAtivasOrdenadas() {
        Questao questao = criarQuestao(1L);
        Alternativa primeira = criarAlternativa(
                1L,
                questao,
                "Primeira alternativa",
                false,
                (short) 1,
                true
        );
        Alternativa segunda = criarAlternativa(
                2L,
                questao,
                "Segunda alternativa",
                true,
                (short) 2,
                true
        );
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findAllByQuestaoIdAndAtivaTrueOrderByOrdemAsc(1L))
                .thenReturn(List.of(primeira, segunda));

        List<AlternativaResponse> resultado = alternativaService.listarAtivas(1L);

        assertEquals(2, resultado.size());
        assertEquals((short) 1, resultado.get(0).ordem());
        assertEquals((short) 2, resultado.get(1).ordem());
    }

    @Test
    void deveBuscarAlternativaPorIdDentroDaQuestao() {
        Questao questao = criarQuestao(1L);
        Alternativa alternativa = criarAlternativa(
                5L,
                questao,
                "Alternativa encontrada",
                false,
                (short) 1,
                true
        );
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findByIdAndQuestaoId(5L, 1L))
                .thenReturn(Optional.of(alternativa));

        AlternativaResponse response = alternativaService.buscarPorId(1L, 5L);

        assertEquals(5L, response.id());
        assertEquals(1L, response.questaoId());
        assertEquals("Alternativa encontrada", response.texto());
    }

    @Test
    void deveInformarQuandoAlternativaNaoPertencerAQuestao() {
        Questao questao = criarQuestao(1L);
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findByIdAndQuestaoId(5L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> alternativaService.buscarPorId(1L, 5L)
        );
    }

    @Test
    void deveAtualizarAlternativaQuandoDadosForemValidos() {
        Questao questao = criarQuestao(1L);
        Alternativa alternativa = criarAlternativa(
                5L,
                questao,
                "Texto antigo",
                false,
                (short) 1,
                true
        );
        AlternativaRequest request = criarRequest(
                "  Texto atualizado  ",
                true,
                (short) 2
        );
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findByIdAndQuestaoId(5L, 1L))
                .thenReturn(Optional.of(alternativa));
        when(alternativaRepository.save(alternativa)).thenReturn(alternativa);

        AlternativaResponse response = alternativaService.atualizar(1L, 5L, request);

        assertEquals("Texto atualizado", response.texto());
        assertEquals(true, response.correta());
        assertEquals((short) 2, response.ordem());
        verify(alternativaRepository).save(alternativa);
    }

    @Test
    void deveRecusarAtualizacaoQuandoOrdemPertencerAOutraAlternativa() {
        Questao questao = criarQuestao(1L);
        Alternativa alternativa = criarAlternativa(
                5L,
                questao,
                "Alternativa",
                false,
                (short) 1,
                true
        );
        AlternativaRequest request = criarRequest("Atualizada", false, (short) 2);
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findByIdAndQuestaoId(5L, 1L))
                .thenReturn(Optional.of(alternativa));
        when(alternativaRepository.existsByQuestaoIdAndOrdemAndIdNot(
                1L,
                (short) 2,
                5L
        )).thenReturn(true);

        assertThrows(
                ConflitoDeDadosException.class,
                () -> alternativaService.atualizar(1L, 5L, request)
        );

        verify(alternativaRepository, never()).save(any(Alternativa.class));
    }

    @Test
    void deveRecusarAtualizacaoQuandoOutraAlternativaCorretaEstiverAtiva() {
        Questao questao = criarQuestao(1L);
        Alternativa alternativa = criarAlternativa(
                5L,
                questao,
                "Alternativa",
                false,
                (short) 1,
                true
        );
        AlternativaRequest request = criarRequest("Nova correta", true, (short) 1);
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findByIdAndQuestaoId(5L, 1L))
                .thenReturn(Optional.of(alternativa));
        when(alternativaRepository
                .existsByQuestaoIdAndCorretaTrueAndAtivaTrueAndIdNot(1L, 5L))
                .thenReturn(true);

        assertThrows(
                ConflitoDeDadosException.class,
                () -> alternativaService.atualizar(1L, 5L, request)
        );

        verify(alternativaRepository, never()).save(any(Alternativa.class));
    }

    @Test
    void deveDesativarAlternativaSemExcluiLa() {
        Questao questao = criarQuestao(1L);
        Alternativa alternativa = criarAlternativa(
                5L,
                questao,
                "Alternativa",
                true,
                (short) 1,
                true
        );
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findByIdAndQuestaoId(5L, 1L))
                .thenReturn(Optional.of(alternativa));
        when(alternativaRepository.save(alternativa)).thenReturn(alternativa);

        AlternativaResponse response = alternativaService.desativar(1L, 5L);

        assertFalse(response.ativa());
        assertFalse(alternativa.getAtiva());
        verify(alternativaRepository).save(alternativa);
        verify(alternativaRepository, never()).delete(any(Alternativa.class));
    }

    private AlternativaRequest criarRequest(
            String texto,
            boolean correta,
            short ordem
    ) {
        return new AlternativaRequest(texto, null, correta, ordem);
    }

    private Questao criarQuestao(Long id) {
        Questao questao = new Questao();
        questao.setId(id);
        return questao;
    }

    private Alternativa criarAlternativa(
            Long id,
            Questao questao,
            String texto,
            boolean correta,
            short ordem,
            boolean ativa
    ) {
        OffsetDateTime agora = OffsetDateTime.now();
        Alternativa alternativa = new Alternativa();
        alternativa.setId(id);
        alternativa.setQuestao(questao);
        alternativa.setTexto(texto);
        alternativa.setCorreta(correta);
        alternativa.setOrdem(ordem);
        alternativa.setAtiva(ativa);
        alternativa.setCriadoEm(agora);
        alternativa.setAtualizadoEm(agora);
        return alternativa;
    }
}
