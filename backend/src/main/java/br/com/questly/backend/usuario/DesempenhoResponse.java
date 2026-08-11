package br.com.questly.backend.usuario;

import java.util.List;

public record DesempenhoResponse(
        Long totalTentativas,
        Long acertos,
        Integer taxaAcerto,
        List<DesempenhoCategoriaResponse> estatisticasPorCategoria
) {
}
