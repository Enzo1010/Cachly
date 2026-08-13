package br.com.cachly.backend.simulador;

import java.util.List;

public record SimulacaoResponse(
    int bitsOffset,
    int bitsIndice,
    int bitsTag,
    int totalLinhas,
    int totalConjuntos,
    int totalAcessos,
    int totalHits,
    int totalMisses,
    double taxaHit,
    double taxaMiss,
    List<PassoSimulacaoResponse> passos
) {}
