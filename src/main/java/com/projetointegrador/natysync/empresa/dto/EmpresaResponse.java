package com.projetointegrador.natysync.empresa.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmpresaResponse(
        UUID id, String nome, boolean ativa, OffsetDateTime criadoEm, OffsetDateTime atualizadoEm) {}
