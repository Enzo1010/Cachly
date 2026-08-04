package br.com.questly.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlternativaRequest(
        @NotBlank(message = "O texto da alternativa é obrigatório")
        String texto,

        @NotNull(message = "A indicação de alternativa correta é obrigatória")
        Boolean correta,

        @NotNull(message = "A ordem da alternativa é obrigatória")
        @Positive(message = "A ordem da alternativa deve ser maior que zero")
        Short ordem
) {
}
