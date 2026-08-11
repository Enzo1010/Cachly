package br.com.questly.backend.usuario.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlunoCadastroRequest(
        @NotBlank(message = "O nome do aluno é obrigatório")
        @Size(max = 100, message = "O nome do aluno deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail do aluno é obrigatório")
        @Email(message = "O e-mail do aluno deve ter um formato válido")
        @Size(max = 150, message = "O e-mail do aluno deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "A senha do aluno é obrigatória")
        @Size(
                min = 8,
                max = 72,
                message = "A senha do aluno deve ter entre 8 e 72 caracteres"
        )
        String senha
) {
}
