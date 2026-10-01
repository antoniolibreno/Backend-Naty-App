package com.projetointegrador.natysync.shared.exception;

import lombok.Getter;

@Getter
public class TentativaQuizInvalidaException extends RuntimeException {

    private final String campo;

    public TentativaQuizInvalidaException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }
}
