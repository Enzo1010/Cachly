package br.com.cachly.backend.simulador;

import java.util.List;

public record PassoSimulacaoResponse(
    int passo,
    int endereco,
    int tag,
    Integer indice,
    int offset,
    boolean hit,
    Integer blocoSubstituido,
    List<EstadoLinhaCacheResponse> estadoCache
) {}
