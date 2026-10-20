package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.comum.erro.RegraNegocioException;
import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.simulador.PoliticaSubstituicao;
import br.com.cachly.backend.simulador.SimulacaoRequest;
import br.com.cachly.backend.simulador.SimuladorCacheService;
import br.com.cachly.backend.simulador.TipoMapeamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDesafioSimuladorServiceTest {

    @Mock
    private DesafioSimuladorRepository repository;

    @Spy
    private SimuladorCacheService simuladorCacheService = new SimuladorCacheService();

    @InjectMocks
    private AdminDesafioSimuladorService service;

    @Test
    @DisplayName("Deve rejeitar desafio cuja opção correta não existe")
    void deveRejeitarOpcaoCorretaAusente() {
        when(repository.existsById("cache-basica")).thenReturn(false);

        DesafioSimuladorRequest request = criarRequest(List.of(
                new OpcaoDesafioRequest("A", "Resposta A"),
                new OpcaoDesafioRequest("B", "Resposta B")
        ), "C");

        assertThrows(RegraNegocioException.class, () -> service.cadastrar(request));
        verify(simuladorCacheService, never()).executarSimulacao(request.configuracao());
    }

    @Test
    @DisplayName("Deve rejeitar IDs de opções repetidos sem considerar maiúsculas")
    void deveRejeitarOpcoesComIdsDuplicados() {
        when(repository.existsById("cache-basica")).thenReturn(false);

        DesafioSimuladorRequest request = criarRequest(List.of(
                new OpcaoDesafioRequest("A", "Resposta A"),
                new OpcaoDesafioRequest("a", "Resposta repetida")
        ), "A");

        assertThrows(RegraNegocioException.class, () -> service.cadastrar(request));
        verify(simuladorCacheService, never()).executarSimulacao(request.configuracao());
    }

    private DesafioSimuladorRequest criarRequest(List<OpcaoDesafioRequest> opcoes, String opcaoCorretaId) {
        SimulacaoRequest configuracao = new SimulacaoRequest(
                64,
                16,
                null,
                TipoMapeamento.DIRETO,
                PoliticaSubstituicao.LRU,
                List.of(0, 16)
        );

        return new DesafioSimuladorRequest(
                "cache-basica",
                "Cache básica",
                "Desafio de validação",
                DificuldadeQuestao.FACIL,
                10,
                configuracao,
                "Qual opção é correta?",
                opcoes,
                opcaoCorretaId,
                "Leia o enunciado com atenção",
                "Explicação da resposta"
        );
    }
}
