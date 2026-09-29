package com.projetointegrador.natysync.painel.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmpresaResponse(
        UUID id,
        String nome,
        boolean ativa,
        String fusoHorario,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {}
