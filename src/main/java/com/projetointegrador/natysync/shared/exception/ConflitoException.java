package com.projetointegrador.natysync.shared.exception;

import lombok.Getter;

@Getter
public class ConflitoException extends RuntimeException {

    private final String codigo;

    public ConflitoException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }
}
