package br.com.questly.backend.resposta;

import br.com.questly.backend.questao.DificuldadeQuestao;
import br.com.questly.backend.questao.Questao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XpServiceTest {

    private final XpService xpService = new XpService();

    // --- calcularXpGanho ---

    @Test
    void deveRetornarXpBaseParaQuestaoFacil() {
        Questao questao = criarQuestao(DificuldadeQuestao.FACIL, 10);
        assertEquals(10, xpService.calcularXpGanho(questao));
    }

    @Test
    void deveRetornarDuasVezesXpBaseParaQuestaoMedia() {
        Questao questao = criarQuestao(DificuldadeQuestao.MEDIO, 10);
        assertEquals(20, xpService.calcularXpGanho(questao));
    }

    @Test
    void deveRetornarTresVezesXpBaseParaQuestaoDificil() {
        Questao questao = criarQuestao(DificuldadeQuestao.DIFICIL, 10);
        assertEquals(30, xpService.calcularXpGanho(questao));
    }

    // --- calcularNivel ---

    @Test
    void deveRetornarNivelUmParaZeroXp() {
        assertEquals(1, xpService.calcularNivel(0));
    }

    @Test
    void deveRetornarNivelUmParaXpAbaixoDoLimiarDoNivelDois() {
        assertEquals(1, xpService.calcularNivel(99));
    }

    @Test
    void deveRetornarNivelDoisAoAtingirCemXp() {
        assertEquals(2, xpService.calcularNivel(100));
    }

    @Test
    void deveRetornarNivelDoisParaXpAbaixoDoLimiarDoNivelTres() {
        assertEquals(2, xpService.calcularNivel(249));
    }

    @Test
    void deveRetornarNivelTresAoAtingirDuzentosECinquentaXp() {
        assertEquals(3, xpService.calcularNivel(250));
    }

    @Test
    void deveRetornarNivelQuatroAoAtingirQuatrocentosECinquentaXp() {
        assertEquals(4, xpService.calcularNivel(450));
    }

    @Test
    void deveRetornarNiveCincoAoAtingirSeteCentosXp() {
        assertEquals(5, xpService.calcularNivel(700));
    }

    private Questao criarQuestao(DificuldadeQuestao dificuldade, int xpBase) {
        Questao questao = new Questao();
        questao.setDificuldade(dificuldade);
        questao.setXpBase(xpBase);
        return questao;
    }
}
