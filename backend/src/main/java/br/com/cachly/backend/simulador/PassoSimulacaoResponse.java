package br.com.cachly.backend.simulador;

import java.util.List;

public record PassoSimulacaoResponse(
    int passoNumero,
    int endereco,
    int tag,
    Integer indice,
    int offset,
    boolean hit,
    Integer blocoSubstituido,
    EstadoLinhaCacheResponse deltaLinha
) {}
