package br.com.questly.backend.usuario.autenticacao;

import br.com.questly.backend.usuario.PerfilUsuario;

public record UsuarioAutenticadoResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Integer xpTotal,
        Integer nivel,
        String token
) {
}
