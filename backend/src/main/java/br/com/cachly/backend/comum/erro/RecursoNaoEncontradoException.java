package br.com.cachly.backend.comum.erro;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
