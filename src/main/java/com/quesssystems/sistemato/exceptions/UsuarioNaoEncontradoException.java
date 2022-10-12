package com.quesssystems.sistemato.exceptions;

public class UsuarioNaoEncontradoException extends Exception {
    public UsuarioNaoEncontradoException() {
        super("Não existe usuário logado");
    }

    public UsuarioNaoEncontradoException(Integer id) {
        super(String.format("Usuário %d não encontrado", id));
    }
}
