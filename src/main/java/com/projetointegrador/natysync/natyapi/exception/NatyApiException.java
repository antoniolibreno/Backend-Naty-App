package com.projetointegrador.natysync.natyapi.exception;

public class NatyApiException extends RuntimeException {

    public NatyApiException(String mensagem) {
        super(mensagem);
    }

    public NatyApiException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
