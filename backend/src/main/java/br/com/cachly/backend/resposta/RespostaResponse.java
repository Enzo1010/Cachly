package br.com.cachly.backend.resposta;

public record RespostaResponse(
        Long tentativaId,
        Boolean correta,
        String explicacao,
        String explicacaoAlternativa,
        Integer xpConcedido,
        Integer nivelAtual,
        Integer xpTotal
) {
}
