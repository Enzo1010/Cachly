package br.com.questly.backend.usuario;

public record UsuarioSessaoResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Integer xpTotal,
        Integer nivel
) {
}
