package br.com.questly.backend.questao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuestaoRequest(
        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        @NotBlank(message = "O enunciado é obrigatório")
        String enunciado,

        @NotBlank(message = "A explicação é obrigatória")
        String explicacao,

        @NotNull(message = "A dificuldade é obrigatória")
        DificuldadeQuestao dificuldade,

        @NotNull(message = "O XP base é obrigatório")
        @Positive(message = "O XP base deve ser maior que zero")
        Integer xpBase
) {
}
