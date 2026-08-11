package br.com.cachly.backend.usuario.aluno;

public record RankingResponse(
        int posicao,
        String nome,
        int nivel,
        int xpTotal
) {
}
