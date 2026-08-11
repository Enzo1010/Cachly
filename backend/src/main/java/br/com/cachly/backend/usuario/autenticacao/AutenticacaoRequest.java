package br.com.cachly.backend.usuario.autenticacao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AutenticacaoRequest(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve ser válido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}
