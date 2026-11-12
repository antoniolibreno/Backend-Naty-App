package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Ativacao da trilha. Trilha inativa sai da listagem e mantem o historico de quem a estudou")
public record AtivacaoRequest(
        @Schema(
                description = "Verdadeiro ativa a trilha, falso a desativa",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean ativa) {}
