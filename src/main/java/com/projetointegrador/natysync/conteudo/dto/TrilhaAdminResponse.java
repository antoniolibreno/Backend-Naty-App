package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Trilha de treinamento. O conteudo e global e vale para todas as empresas")
public record TrilhaAdminResponse(
        @Schema(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
        UUID id,

        @Schema(description = "Titulo da trilha", example = "Onboarding Naty")
        String titulo,

        @Schema(description = "Descricao livre da trilha", example = "Conceitos iniciais e primeiros passos")
        String descricao,

        @Schema(description = "Posicao entre as trilhas, a partir de 1", example = "1")
        Integer ordem,

        @Schema(description = "Trilha inativa sai da listagem e continua legivel por identificador", example = "true")
        boolean ativa) {}
