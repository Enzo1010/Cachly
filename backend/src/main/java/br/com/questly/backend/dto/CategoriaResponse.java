package br.com.questly.backend.dto;

import java.time.OffsetDateTime;

public record CategoriaResponse(
        Long id,
        String nome,
        String descricao,
        Boolean ativa,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
