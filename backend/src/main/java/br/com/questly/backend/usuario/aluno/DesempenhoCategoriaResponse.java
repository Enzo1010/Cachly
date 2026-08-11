package br.com.questly.backend.usuario.aluno;

public record DesempenhoCategoriaResponse(
        Long categoriaId,
        String categoriaNome,
        Long totalTentativas,
        Long acertos,
        Integer taxaAcerto
) {
}
