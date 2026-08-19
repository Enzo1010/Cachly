package br.com.cachly.backend.alternativa;

import java.time.OffsetDateTime;

public record AlternativaResponse(
        Long id,
        Long questaoId,
        String texto,
        Boolean correta,
        Short ordem,
        Boolean ativa,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm,
        Long criadoPor,
        Long atualizadoPor
) {
}
