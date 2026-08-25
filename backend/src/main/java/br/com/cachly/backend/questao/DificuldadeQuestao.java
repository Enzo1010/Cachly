package br.com.cachly.backend.questao;

public enum DificuldadeQuestao {
    FACIL(1),
    MEDIO(2),
    DIFICIL(3);

    private final int multiplicador;

    DificuldadeQuestao(int multiplicador) {
        this.multiplicador = multiplicador;
    }

    public int getMultiplicador() {
        return multiplicador;
    }
}
