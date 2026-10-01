package br.com.cachly.backend.questao.comentario;

import jakarta.validation.constraints.NotBlank;

public record NovoComentarioRequest(
    @NotBlank(message = "O conteúdo do comentário é obrigatório.")
    String conteudo
) {}
