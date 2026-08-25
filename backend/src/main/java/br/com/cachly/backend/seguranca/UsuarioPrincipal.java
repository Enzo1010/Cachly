package br.com.cachly.backend.seguranca;

public record UsuarioPrincipal(
        Long id,
        String email,
        Long iat
) {
}
