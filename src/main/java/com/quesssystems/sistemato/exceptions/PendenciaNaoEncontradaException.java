package com.quesssystems.sistemato.exceptions;

public class PendenciaNaoEncontradaException extends Exception {
    public PendenciaNaoEncontradaException(Integer id) {
        super(String.format("Pendência %d não encontrada", id));
    }
}
