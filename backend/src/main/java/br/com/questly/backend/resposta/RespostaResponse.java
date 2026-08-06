package br.com.questly.backend.resposta;

public record RespostaResponse(
        Long tentativaId,
        Boolean correta,
        String explicacao,
        Integer xpConcedido
) {
}
