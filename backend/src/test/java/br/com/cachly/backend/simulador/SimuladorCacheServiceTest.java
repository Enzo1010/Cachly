package br.com.cachly.backend.simulador;

import br.com.cachly.backend.comum.erro.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimuladorCacheServiceTest {

    private SimuladorCacheService service;

    @BeforeEach
    void setUp() {
        service = new SimuladorCacheService();
    }

    @Test
    @DisplayName("Deve simular Mapeamento Direto com sucesso")
    void deveSimularMapeamentoDireto() {
        // Cache 16 bytes, Bloco 4 bytes = 4 linhas
        SimulacaoRequest request = new SimulacaoRequest(
                16,
                4,
                null,
                TipoMapeamento.DIRETO,
                null,
                List.of(0, 4, 8, 12, 0, 4)
        );

        SimulacaoResponse response = service.executarSimulacao(request);

        assertEquals(2, response.bitsOffset()); // log2(4) = 2
        assertEquals(2, response.bitsIndice()); // log2(4) = 2
        assertEquals(28, response.bitsTag());   // 32 - 2 - 2 = 28
        assertEquals(4, response.totalLinhas());
        assertEquals(4, response.totalConjuntos());
        assertEquals(6, response.totalAcessos());
        assertEquals(2, response.totalHits());
        assertEquals(4, response.totalMisses());
        assertEquals(33.33, response.taxaHit());
        assertEquals(66.67, response.taxaMiss());

        // Verificando os passos
        assertFalse(response.passos().get(0).hit()); // 0 -> Miss
        assertFalse(response.passos().get(1).hit()); // 4 -> Miss
        assertFalse(response.passos().get(2).hit()); // 8 -> Miss
        assertFalse(response.passos().get(3).hit()); // 12 -> Miss
        assertTrue(response.passos().get(4).hit());  // 0 -> Hit
        assertTrue(response.passos().get(5).hit());  // 4 -> Hit
    }

    @Test
    @DisplayName("Deve simular Totalmente Associativo com politica LRU")
    void deveSimularTotalmenteAssociativoLRU() {
        // Cache 8 bytes, Bloco 4 bytes = 2 linhas
        SimulacaoRequest request = new SimulacaoRequest(
                8,
                4,
                null,
                TipoMapeamento.TOTALMENTE_ASSOCIATIVO,
                PoliticaSubstituicao.LRU,
                List.of(0, 4, 8, 0)
        );

        SimulacaoResponse response = service.executarSimulacao(request);

        assertEquals(2, response.bitsOffset()); // log2(4) = 2
        assertEquals(0, response.bitsIndice()); // 0
        assertEquals(30, response.bitsTag());   // 32 - 2 = 30
        assertEquals(2, response.totalLinhas());
        assertEquals(1, response.totalConjuntos());
        assertEquals(4, response.totalMisses());

        // Passo 3: acesso 8 substitui linha 0 (pois 0 foi acessado antes de 4)
        assertEquals(0, response.passos().get(2).blocoSubstituido());
        // Passo 4: acesso 0 substitui linha 1 (pois 4 foi o menos recentemente usado)
        assertEquals(1, response.passos().get(3).blocoSubstituido());
    }

    @Test
    @DisplayName("Deve simular Totalmente Associativo com politica FIFO")
    void deveSimularTotalmenteAssociativoFIFO() {
        // Cache 8 bytes, Bloco 4 bytes = 2 linhas
        SimulacaoRequest request = new SimulacaoRequest(
                8,
                4,
                null,
                TipoMapeamento.TOTALMENTE_ASSOCIATIVO,
                PoliticaSubstituicao.FIFO,
                List.of(0, 4, 0, 8)
        );

        SimulacaoResponse response = service.executarSimulacao(request);

        // Acesso 0 -> Miss (linha 0, ordem=1)
        // Acesso 4 -> Miss (linha 1, ordem=2)
        // Acesso 0 -> Hit (linha 0 atualizada ultUtilizacao=3, mas ordem=1 mantida)
        // Acesso 8 -> Miss! Em FIFO, a linha mais antiga é a linha 0 (ordem=1), mesmo tendo sido acessada recentemente
        assertEquals(0, response.passos().get(3).blocoSubstituido());
    }

    @Test
    @DisplayName("Deve lançar exceção se tamanho da cache não for potência de 2")
    void deveLancarExcecaoParaTamanhoCacheInvalido() {
        SimulacaoRequest request = new SimulacaoRequest(
                15,
                4,
                null,
                TipoMapeamento.DIRETO,
                null,
                List.of(0)
        );

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.executarSimulacao(request)
        );
        assertEquals("Tamanho da cache deve ser uma potência de 2", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção se bloco for maior que a cache")
    void deveLancarExcecaoParaBlocoMaiorQueCache() {
        SimulacaoRequest request = new SimulacaoRequest(
                8,
                16,
                null,
                TipoMapeamento.DIRETO,
                null,
                List.of(0)
        );

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.executarSimulacao(request)
        );
        assertEquals("Tamanho do bloco não pode ser maior que o tamanho da cache", exception.getMessage());
    }
}
