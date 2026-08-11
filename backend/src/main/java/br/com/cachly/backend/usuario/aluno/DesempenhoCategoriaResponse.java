package br.com.cachly.backend.usuario.aluno;

public record DesempenhoCategoriaResponse(
        Long categoriaId,
        String categoriaNome,
        Long totalTentativas,
        Long acertos,
        Integer taxaAcerto
) {
}
