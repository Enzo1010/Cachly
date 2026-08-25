package br.com.cachly.backend.seguranca;

public record UsuarioPrincipal(
        String email,
        Long iat
) {
}
