package br.com.cachly.backend.simulador.desafio;

import jakarta.validation.constraints.NotBlank;

public record OpcaoDesafioRequest(
        @NotBlank(message = "A letra da opção (ex: A) é obrigatória")
        String id,

        @NotBlank(message = "O texto da opção é obrigatório")
        String texto
) {}
