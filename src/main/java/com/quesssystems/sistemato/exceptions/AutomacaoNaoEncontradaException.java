package com.quesssystems.sistemato.exceptions;

public class AutomacaoNaoEncontradaException extends Exception {
    public AutomacaoNaoEncontradaException(Integer id) {
        super(String.format("Automação %d não encontrada", id));
    }
}
