package com.projetointegrador.natysync.progresso.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Modulo com o estado de cada atividade para o integrante")
public record ModuloProgressoResponse(
        @Schema(description = "Identificador do modulo", example = "5d1f8a63-7e2c-4b90-a4d5-1f3b9c6e8a07")
        UUID id,

        @Schema(description = "Titulo do modulo", example = "Primeiros passos")
        String titulo,

        @Schema(description = "Descricao do modulo", example = "Como receber e organizar as primeiras conversas")
        String descricao,

        @Schema(description = "Posicao do modulo na trilha, a partir de 1", example = "1")
        Integer ordem,

        @Schema(description = "Atividades do modulo em ordem crescente, com o estado de cada uma")
        List<AtividadeProgressoResponse> atividades) {}
