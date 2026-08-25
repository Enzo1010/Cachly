package br.com.cachly.backend.questao;

import br.com.cachly.backend.alternativa.AlternativaResponse;

import java.time.OffsetDateTime;
import java.util.List;

public record QuestaoResponse(
        Long id,
        Long categoriaId,
        String categoriaNome,
        String enunciado,
        String explicacao,
        DificuldadeQuestao dificuldade,
        Integer xpBase,
        Boolean ativa,
        List<AlternativaResponse> alternativas,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm,
        Long criadoPor,
        Long atualizadoPor
) {
}
