package br.com.questly.backend.resposta;

import jakarta.validation.constraints.NotNull;

public record RespostaRequest(
        @NotNull(message = "O ID da alternativa é obrigatório")
        Long alternativaId
) {
}
