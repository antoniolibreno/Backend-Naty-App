package com.projetointegrador.natysync.usuario.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessaoResponse(
        UUID usuarioId, UUID empresaId, String nome, String email, String token, OffsetDateTime expiraEm) {}
