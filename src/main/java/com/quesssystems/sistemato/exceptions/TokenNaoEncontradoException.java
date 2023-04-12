package com.quesssystems.sistemato.exceptions;

public class TokenNaoEncontradoException extends Exception {
    public TokenNaoEncontradoException(Integer id) {
        super(String.format("Token %d não encontrado", id));
    }
}
