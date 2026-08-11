package br.com.questly.backend.usuario;

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
