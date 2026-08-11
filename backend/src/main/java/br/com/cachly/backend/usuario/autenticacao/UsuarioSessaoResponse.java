package br.com.cachly.backend.usuario.autenticacao;

import br.com.cachly.backend.usuario.PerfilUsuario;

public record UsuarioSessaoResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Integer xpTotal,
        Integer nivel
) {
}
