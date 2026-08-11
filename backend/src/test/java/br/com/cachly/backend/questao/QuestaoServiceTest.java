package br.com.cachly.backend.questao;

import br.com.cachly.backend.alternativa.Alternativa;
import br.com.cachly.backend.alternativa.AlternativaRepository;
import br.com.cachly.backend.categoria.Categoria;
import br.com.cachly.backend.categoria.CategoriaRepository;
import br.com.cachly.backend.comum.erro.ConflitoDeDadosException;
import br.com.cachly.backend.comum.erro.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
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
class QuestaoServiceTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private AlternativaRepository alternativaRepository;

    @InjectMocks
    private QuestaoService questaoService;

    @Test
    void deveCadastrarQuestaoQuandoCategoriaEstiverAtiva() {
        Categoria categoria = criarCategoria(1L, true);
        QuestaoRequest request = criarRequest(1L);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(questaoRepository.save(any(Questao.class)))
                .thenAnswer(invocacao -> {
                    Questao questao = invocacao.getArgument(0);
                    OffsetDateTime agora = OffsetDateTime.now();
                    questao.setId(10L);
                    questao.setCriadoEm(agora);
                    questao.setAtualizadoEm(agora);
                    return questao;
                });

        QuestaoResponse response = questaoService.cadastrar(request);

        ArgumentCaptor<Questao> captor = ArgumentCaptor.forClass(Questao.class);
        verify(questaoRepository).save(captor.capture());

        assertEquals(10L, response.id());
        assertEquals(1L, response.categoriaId());
        assertEquals("O que é uma porta AND?", response.enunciado());
        assertEquals("Uma porta que realiza conjunção lógica.", response.explicacao());
        assertEquals(DificuldadeQuestao.FACIL, response.dificuldade());
        assertEquals(10, response.xpBase());
        assertEquals(categoria, captor.getValue().getCategoria());
    }

    @Test
    void deveRecusarCadastroQuandoCategoriaNaoExistir() {
        QuestaoRequest request = criarRequest(99L);
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> questaoService.cadastrar(request)
        );

        verify(questaoRepository, never()).save(any(Questao.class));
    }

    @Test
    void deveRecusarCadastroQuandoCategoriaEstiverInativa() {
        Categoria categoria = criarCategoria(1L, false);
        QuestaoRequest request = criarRequest(1L);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        assertThrows(
                ConflitoDeDadosException.class,
                () -> questaoService.cadastrar(request)
        );

        verify(questaoRepository, never()).save(any(Questao.class));
    }

    @Test
    void deveListarQuestoesAtivas() {
        Categoria categoria = criarCategoria(1L, true);
        Questao primeira = criarQuestao(1L, categoria, "Primeira questão", true);
        Questao segunda = criarQuestao(2L, categoria, "Segunda questão", true);
        when(questaoRepository.findAllByAtivaTrueOrderByIdAsc())
                .thenReturn(List.of(primeira, segunda));

        List<QuestaoResponse> resultado = questaoService.listarAtivas();

        assertEquals(2, resultado.size());
        assertEquals("Primeira questão", resultado.get(0).enunciado());
        assertEquals("Segunda questão", resultado.get(1).enunciado());
    }

    @Test
    void deveListarQuestoesParaEstudoSemRevelarAAlternativaCorreta() {
        Categoria categoria = criarCategoria(1L, true);
        Questao questao = criarQuestao(1L, categoria, "O que é um bit?", true);
        
        Alternativa altCorreta = new Alternativa();
        altCorreta.setId(10L);
        altCorreta.setTexto("0 ou 1");
        altCorreta.setCorreta(true);
        altCorreta.setOrdem((short) 1);
        
        Alternativa altIncorreta = new Alternativa();
        altIncorreta.setId(11L);
        altIncorreta.setTexto("8 bytes");
        altIncorreta.setCorreta(false);
        altIncorreta.setOrdem((short) 2);

        PageRequest pageRequest = PageRequest.of(0, 10);
        when(questaoRepository.findAllByCategoriaIdAndAtivaTrueOrderByIdAsc(1L, pageRequest))
                .thenReturn(List.of(questao));
                
        when(alternativaRepository.findAllByQuestaoIdAndAtivaTrueOrderByOrdemAsc(1L))
                .thenReturn(List.of(altCorreta, altIncorreta));

        List<QuestaoEstudoResponse> resultado = questaoService.listarParaEstudo(1L, 10);

        assertEquals(1, resultado.size());
        QuestaoEstudoResponse response = resultado.get(0);
        assertEquals(1L, response.id());
        assertEquals("O que é um bit?", response.enunciado());
        
        assertEquals(2, response.alternativas().size());
        assertEquals(10L, response.alternativas().get(0).id());
        assertEquals("0 ou 1", response.alternativas().get(0).texto());
        
        assertEquals(11L, response.alternativas().get(1).id());
        assertEquals("8 bytes", response.alternativas().get(1).texto());
    }

    @Test
    void deveBuscarQuestaoPorId() {
        Categoria categoria = criarCategoria(1L, true);
        Questao questao = criarQuestao(5L, categoria, "Questão encontrada", true);
        when(questaoRepository.findById(5L)).thenReturn(Optional.of(questao));

        QuestaoResponse response = questaoService.buscarPorId(5L);

        assertEquals(5L, response.id());
        assertEquals("Questão encontrada", response.enunciado());
    }

    @Test
    void deveInformarQuandoQuestaoNaoForEncontrada() {
        when(questaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> questaoService.buscarPorId(99L)
        );
    }

    @Test
    void deveAtualizarQuestaoQuandoCategoriaEstiverAtiva() {
        Categoria categoriaAtual = criarCategoria(1L, true);
        Categoria novaCategoria = criarCategoria(2L, true);
        Questao questao = criarQuestao(5L, categoriaAtual, "Enunciado antigo", true);
        QuestaoRequest request = criarRequest(2L);

        when(questaoRepository.findById(5L)).thenReturn(Optional.of(questao));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(novaCategoria));
        when(questaoRepository.save(questao)).thenReturn(questao);

        QuestaoResponse response = questaoService.atualizar(5L, request);

        assertEquals(2L, response.categoriaId());
        assertEquals("O que é uma porta AND?", response.enunciado());
        assertEquals(novaCategoria, questao.getCategoria());
        verify(questaoRepository).save(questao);
    }

    @Test
    void deveDesativarQuestaoSemExcluiLa() {
        Categoria categoria = criarCategoria(1L, true);
        Questao questao = criarQuestao(5L, categoria, "Pipeline", true);
        when(questaoRepository.findById(5L)).thenReturn(Optional.of(questao));
        when(questaoRepository.save(questao)).thenReturn(questao);

        QuestaoResponse response = questaoService.desativar(5L);

        assertFalse(response.ativa());
        assertFalse(questao.getAtiva());
        verify(questaoRepository).save(questao);
        verify(questaoRepository, never()).delete(any(Questao.class));
    }

    private QuestaoRequest criarRequest(Long categoriaId) {
        return new QuestaoRequest(
                categoriaId,
                "  O que é uma porta AND?  ",
                "  Uma porta que realiza conjunção lógica.  ",
                DificuldadeQuestao.FACIL,
                10
        );
    }

    private Categoria criarCategoria(Long id, boolean ativa) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome("Portas Lógicas");
        categoria.setAtiva(ativa);
        return categoria;
    }

    private Questao criarQuestao(
            Long id,
            Categoria categoria,
            String enunciado,
            boolean ativa
    ) {
        OffsetDateTime agora = OffsetDateTime.now();
        Questao questao = new Questao();
        questao.setId(id);
        questao.setCategoria(categoria);
        questao.setEnunciado(enunciado);
        questao.setExplicacao("Explicação da resposta");
        questao.setDificuldade(DificuldadeQuestao.FACIL);
        questao.setXpBase(10);
        questao.setAtiva(ativa);
        questao.setCriadoEm(agora);
        questao.setAtualizadoEm(agora);
        return questao;
    }
}
