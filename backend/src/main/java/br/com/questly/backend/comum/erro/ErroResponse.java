package br.com.questly.backend.comum.erro;

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
