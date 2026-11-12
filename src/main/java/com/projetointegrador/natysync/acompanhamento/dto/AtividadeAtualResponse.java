package com.projetointegrador.natysync.acompanhamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Primeira atividade ativa, na ordem da trilha, que o integrante nao concluiu")
public record AtividadeAtualResponse(
        @Schema(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
        UUID atividadeId,

        @Schema(description = "Identificador da trilha da atividade", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
        UUID trilhaId,

        @Schema(description = "Titulo da atividade", example = "Como cadastrar um contato")
        String titulo) {}
