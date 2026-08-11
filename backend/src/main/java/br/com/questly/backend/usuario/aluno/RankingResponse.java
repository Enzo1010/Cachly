package br.com.questly.backend.usuario.aluno;

public record RankingResponse(
        int posicao,
        String nome,
        int nivel,
        int xpTotal
) {
}
