package br.com.cachly.backend.simulador.desafio;

import jakarta.validation.constraints.NotBlank;

public record VerificarDesafioRequest(
        @NotBlank(message = "A alternativa selecionada é obrigatória")
        String opcaoSelecionadaId
) {}
