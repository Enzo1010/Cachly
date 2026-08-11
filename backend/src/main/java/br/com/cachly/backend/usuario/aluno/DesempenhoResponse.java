package br.com.cachly.backend.usuario.aluno;

import java.util.List;

public record DesempenhoResponse(
        Long totalTentativas,
        Long acertos,
        Integer taxaAcerto,
        List<DesempenhoCategoriaResponse> estatisticasPorCategoria
) {
}
