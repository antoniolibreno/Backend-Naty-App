package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Ativação da trilha")
public record AtivacaoRequest(
        @Schema(
                description = "Verdadeiro ativa, falso desativa",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean ativa) {}
