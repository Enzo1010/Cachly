package br.com.questly.backend.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErroResponse(
        OffsetDateTime momento,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {
}
