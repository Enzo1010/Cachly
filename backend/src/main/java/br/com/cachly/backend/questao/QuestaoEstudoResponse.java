package br.com.cachly.backend.questao;

import java.util.List;

public record QuestaoEstudoResponse(
        Long id,
        String enunciado,
        DificuldadeQuestao dificuldade,
        Integer xpBase,
        List<AlternativaEstudoResponse> alternativas
) {
}
