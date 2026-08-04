package br.com.questly.backend.usuario;

import java.time.OffsetDateTime;

public record AlunoResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Integer xpTotal,
        Integer nivel,
        Boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
