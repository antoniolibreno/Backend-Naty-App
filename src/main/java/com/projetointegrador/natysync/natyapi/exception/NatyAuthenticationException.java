package com.projetointegrador.natysync.natyapi.exception;

public class NatyAuthenticationException extends NatyApiException {

    public NatyAuthenticationException(String mensagem) {
        super(mensagem);
    }

    public NatyAuthenticationException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
