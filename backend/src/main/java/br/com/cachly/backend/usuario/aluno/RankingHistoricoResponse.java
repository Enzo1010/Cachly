package br.com.cachly.backend.usuario.aluno;

import java.time.LocalDate;

public record RankingHistoricoResponse(
        String nomeUsuario,
        Integer xpFinal,
        Integer posicao,
        LocalDate dataSemana
) {
}
