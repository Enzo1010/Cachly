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

    @Test
    @DisplayName("Deve simular com valor maximo possivel para tamanho de bloco (evitando overflow bitwise)")
    void deveSimularComMaximoBlocoPossivel() {
        // O limite maximo de potência de 2 em Integer positivo é 2^30 = 1073741824
        int maxBloco = 1 << 30;
        SimulacaoRequest request = new SimulacaoRequest(
                maxBloco,
                maxBloco,
                null,
                TipoMapeamento.DIRETO,
                null,
                List.of(maxBloco - 1, maxBloco)
        );

        SimulacaoResponse response = service.executarSimulacao(request);

        assertEquals(30, response.bitsOffset());
        assertEquals(0, response.bitsIndice());
        assertEquals(2, response.bitsTag()); 
        
        // Acesso 1: (maxBloco - 1) tem tag 0, offset maxBloco - 1
        assertEquals(0, response.passos().get(0).tag());
        assertEquals(maxBloco - 1, response.passos().get(0).offset());
        
        // Acesso 2: maxBloco tem tag 1, offset 0
        assertEquals(1, response.passos().get(1).tag());
        assertEquals(0, response.passos().get(1).offset());
    }

    @Test
    @DisplayName("Deve lançar exceção se o número total de linhas exceder o limite máximo (anti-DoS)")
    void deveLancarExcecaoQuandoTotalLinhasExcederLimiteMaximo() {
        // 8192 bytes de cache com bloco de 1 byte = 8192 linhas (> 4096)
        SimulacaoRequest request = new SimulacaoRequest(
                8192,
                1,
                null,
                TipoMapeamento.DIRETO,
                null,
                List.of(0)
        );

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> service.executarSimulacao(request)
        );
        assertTrue(exception.getMessage().contains("excede o limite máximo permitido de 4096"));
    }

    @Test
    @DisplayName("Deve explicar Miss Compulsório e Miss de Conflito (3 Cs) no Mapeamento Direto sem mencionar FIFO ou LRU e sem citar Conjunto")
    void deveExplicarMissDeConflitoNoMapeamentoDiretoSemMencionarPoliticaSubstituicao() {
        // Cache de 8 bytes, bloco de 4 bytes = 2 linhas (linhas 0 e 1)
        // Endereço 0 (bloco 0 -> linha 0, offset 0, tag 0) -> 1º acesso ao bloco 0 (Compulsório em linha vazia)
        // Endereço 8 (bloco 2 -> linha 0, offset 0, tag 1) -> 1º acesso ao bloco 2 (Compulsório substituindo linha ocupada)
        // Endereço 0 (bloco 0 -> linha 0, offset 0, tag 0) -> 2º acesso ao bloco 0 após evicção por colisão (Miss de Conflito)
        SimulacaoRequest request = new SimulacaoRequest(
                8,
                4,
                null,
                TipoMapeamento.DIRETO,
                null,
                List.of(0, 8, 0)
        );

        SimulacaoResponse response = service.executarSimulacao(request);

        PassoSimulacaoResponse passo1 = response.passos().get(0);
        assertTrue(passo1.explicacao().contains("Verificando a Linha 0 para a Tag 0"));
        assertFalse(passo1.explicacao().contains("Conjunto"));

        PassoSimulacaoResponse passoCompulsorioComSubstituicao = response.passos().get(1);
        assertFalse(passoCompulsorioComSubstituicao.hit());
        assertEquals(0, passoCompulsorioComSubstituicao.blocoSubstituido());
        assertTrue(passoCompulsorioComSubstituicao.explicacao().contains("Verificando a Linha 0 para a Tag 1"));
        assertTrue(passoCompulsorioComSubstituicao.explicacao().contains("MISS COMPULSÓRIO!"));
        assertTrue(passoCompulsorioComSubstituicao.explicacao().contains("No Mapeamento Direto não há algoritmo de substituição"));

        PassoSimulacaoResponse passoConflito = response.passos().get(2);
        assertFalse(passoConflito.hit());
        assertEquals(0, passoConflito.blocoSubstituido());
        assertTrue(passoConflito.explicacao().contains("Verificando a Linha 0 para a Tag 0"));
        assertTrue(passoConflito.explicacao().contains("MISS DE CONFLITO!"));
        assertTrue(passoConflito.explicacao().contains("No Mapeamento Direto não há algoritmo de substituição"));
        assertFalse(passoConflito.explicacao().contains("FIFO"), "Explicação não deve mencionar FIFO em mapeamento direto");
        assertFalse(passoConflito.explicacao().contains("LRU"), "Explicação não deve mencionar LRU em mapeamento direto");
        assertFalse(passoConflito.explicacao().contains("Conjunto"), "Explicação de mapeamento direto não deve citar Conjunto");
    }

    @Test
    @DisplayName("Deve classificar primeiro acesso a 64 como Miss Compulsório e retorno a 0 como Miss de Conflito (64B, bloco 2B)")
    void deveClassificarPrimeiroAcessoAoBlocoComoCompulsorioMesmoEmLinhaOcupada() {
        SimulacaoRequest request = new SimulacaoRequest(
                64,
                2,
                null,
                TipoMapeamento.DIRETO,
                null,
                List.of(0, 1, 2, 63, 64, 0)
        );

        SimulacaoResponse response = service.executarSimulacao(request);

        // Passo 5 (índice 4): endereço 64 (bloco 32) nunca foi acessado antes -> Miss Compulsório
        PassoSimulacaoResponse passo64 = response.passos().get(4);
        assertFalse(passo64.hit());
        assertEquals(0, passo64.blocoSubstituido());
        assertTrue(passo64.explicacao().contains("MISS COMPULSÓRIO!"));
        assertFalse(passo64.explicacao().contains("MISS DE CONFLITO!"));

        // Passo 6 (índice 5): endereço 0 (bloco 0) já foi acessado antes e sofreu colisão -> Miss de Conflito
        PassoSimulacaoResponse passoRetorno0 = response.passos().get(5);
        assertFalse(passoRetorno0.hit());
        assertEquals(0, passoRetorno0.blocoSubstituido());
        assertTrue(passoRetorno0.explicacao().contains("MISS DE CONFLITO!"));
    }

    @Test
    @DisplayName("Deve respeitar seleção de Mapeamento Direto na explicação mesmo quando há apenas 1 linha (cache 8B, bloco 8B)")
    void deveRespeitarMapeamentoDiretoComUmaUnicaLinha() {
        SimulacaoRequest request = new SimulacaoRequest(
                8,
                8,
                null,
                TipoMapeamento.DIRETO,
                null,
                List.of(0, 8)
        );

        SimulacaoResponse response = service.executarSimulacao(request);

        assertEquals(1, response.totalLinhas());
        assertEquals(0, response.bitsIndice());
        String explicacaoPasso1 = response.passos().get(0).explicacao();
        assertTrue(explicacaoPasso1.contains("Verificando a Linha 0 para a Tag 0"));
        assertFalse(explicacaoPasso1.contains("Totalmente Associativo"));
    }
}
