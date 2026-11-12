package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Trilha de treinamento")
public record TrilhaAdminResponse(
        @Schema(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
        UUID id,

        @Schema(description = "Título", example = "Onboarding Naty")
        String titulo,

        @Schema(description = "Descrição", example = "Conceitos iniciais e primeiros passos")
        String descricao,

        @Schema(description = "Posição, a partir de 1", example = "1")
        Integer ordem,

        @Schema(description = "Inativa sai da listagem", example = "true")
        boolean ativa) {}
