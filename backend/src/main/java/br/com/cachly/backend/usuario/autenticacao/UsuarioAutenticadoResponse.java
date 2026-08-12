package br.com.cachly.backend.usuario.autenticacao;

import br.com.cachly.backend.usuario.PerfilUsuario;

public record UsuarioAutenticadoResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Integer xpTotal,
        Integer nivel,
        String nomeNivel,
        String token
) {
}
