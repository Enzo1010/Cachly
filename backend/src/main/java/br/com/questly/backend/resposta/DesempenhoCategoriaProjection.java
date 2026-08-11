package br.com.questly.backend.resposta;

public interface DesempenhoCategoriaProjection {
    Long getCategoriaId();
    String getCategoriaNome();
    Long getTotalTentativas();
    Long getAcertos();
}
