package br.com.cachly.backend.usuario.aluno;

import java.time.OffsetDateTime;

public record HistoricoTentativaResponse(
        Long id,
        Long questaoId,
        String questaoEnunciado,
        Long alternativaId,
        String alternativaTexto,
        Boolean correta,
        Integer xpConcedido,
        OffsetDateTime respondidaEm
) {
}
