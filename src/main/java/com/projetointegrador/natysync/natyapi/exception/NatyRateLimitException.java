package com.projetointegrador.natysync.natyapi.exception;

import java.time.Duration;

public class NatyRateLimitException extends NatyApiException {

    private final Duration esperaSugerida;

    public NatyRateLimitException(String mensagem, Duration esperaSugerida) {
        super(mensagem);
        this.esperaSugerida = esperaSugerida;
    }

    public Duration getEsperaSugerida() {
        return esperaSugerida;
    }
}
