package br.com.questly.backend.usuario.autenticacao;

import br.com.questly.backend.usuario.PerfilUsuario;

public record UsuarioSessaoResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Integer xpTotal,
        Integer nivel
) {
}
