package br.com.cachly.backend.simulador.desafio;

import br.com.cachly.backend.simulador.SimulacaoResponse;

public record ResultadoDesafioResponse(
        boolean correto,
        String opcaoCorretaId,
        String explicacao,
        int xpGanho,
        SimulacaoResponse simulacao,
        Integer nivelAtual,
        String nomeNivel,
        Integer xpTotalAtual
) {}
