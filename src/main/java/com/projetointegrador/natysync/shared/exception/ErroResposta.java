package com.projetointegrador.natysync.shared.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ErroResposta(
        OffsetDateTime momento, int status, String codigo, String mensagem, String caminho, List<ErroCampo> campos) {

    public record ErroCampo(String campo, String mensagem) {}
}
