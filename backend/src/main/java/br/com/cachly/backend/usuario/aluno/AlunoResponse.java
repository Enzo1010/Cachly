package br.com.cachly.backend.usuario.aluno;

import br.com.cachly.backend.usuario.PerfilUsuario;
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
