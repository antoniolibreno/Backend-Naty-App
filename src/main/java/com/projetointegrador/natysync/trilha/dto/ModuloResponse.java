package com.projetointegrador.natysync.trilha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Módulo de uma trilha com suas atividades")
public record ModuloResponse(
        @Schema(description = "Identificador do módulo", example = "5d1f8a63-7e2c-4b90-a4d5-1f3b9c6e8a07")
        UUID id,

        @Schema(description = "Título do módulo", example = "Primeiros passos")
        String titulo,

        @Schema(description = "Descrição do módulo", example = "Como receber e organizar as primeiras conversas")
        String descricao,

        @Schema(description = "Posição na trilha, a partir de 1", example = "1")
        Integer ordem,

        @Schema(description = "Atividades em ordem crescente")
        List<AtividadeResumoResponse> atividades) {}
