package com.projetointegrador.natysync.painel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Novo estado de ativacao do integrante")
public record AtivoRequest(
        @Schema(
                description = "true ativa o integrante, false o desativa",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean ativo) {}
