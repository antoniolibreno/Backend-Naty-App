package com.projetointegrador.natysync.usuario.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessaoResponse(
        UUID usuarioId, UUID empresaId, String nome, String email, String token, OffsetDateTime expiraEm) {

    @Override
    public String toString() {
        return "SessaoResponse[usuarioId=" + usuarioId + ", empresaId=" + empresaId + ", nome=" + nome + ", email="
                + email + ", token=***, expiraEm=" + expiraEm + "]";
    }
}
