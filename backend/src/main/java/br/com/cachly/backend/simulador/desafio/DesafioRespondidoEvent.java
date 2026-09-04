package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.usuario.Usuario;

public record DesafioRespondidoEvent(
        Usuario usuario,
        String desafioId,
        int xpRecompensa,
        boolean correta,
        boolean primeiraVezCorreta
) {
}
