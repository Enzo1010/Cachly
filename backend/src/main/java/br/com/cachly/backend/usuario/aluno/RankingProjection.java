package br.com.cachly.backend.usuario.aluno;

public interface RankingProjection {
    Long getId();
    String getNome();
    Integer getNivel();
    Integer getXpSemanal();
    Integer getDiasOfensiva();
}
