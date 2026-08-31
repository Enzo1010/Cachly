package br.com.cachly.backend.resposta;

public record RespostaResponse(
        Long tentativaId,
        Boolean correta,
        Long alternativaCorretaId,
        String explicacao,
        Integer xpConcedido,
        Integer nivelAtual,
        String nomeNivel,
        Integer xpTotal
) {
}

