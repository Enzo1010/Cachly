package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.questao.DificuldadeQuestao;
import br.com.cachly.backend.simulador.SimulacaoRequest;

import java.util.List;

public record DesafioCacheResponse(
        String id,
        String titulo,
        String descricao,
        DificuldadeQuestao dificuldade,
        Integer xpRecompensa,
        SimulacaoRequest configuracao,
        String pergunta,
        List<OpcaoDesafioResponse> opcoes,
        String dica
) {}
