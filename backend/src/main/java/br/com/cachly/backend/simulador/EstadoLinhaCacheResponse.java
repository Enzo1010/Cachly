package br.com.cachly.backend.simulador;

public record EstadoLinhaCacheResponse(
    int indiceLinha,
    Integer conjuntoIndex,
    boolean valida,
    Integer tag
) {}
