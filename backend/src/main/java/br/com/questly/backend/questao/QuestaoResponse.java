package br.com.questly.backend.questao;

import java.time.OffsetDateTime;

public record QuestaoResponse(
        Long id,
        Long categoriaId,
        String categoriaNome,
        String enunciado,
        String explicacao,
        DificuldadeQuestao dificuldade,
        Integer xpBase,
        Boolean ativa,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
