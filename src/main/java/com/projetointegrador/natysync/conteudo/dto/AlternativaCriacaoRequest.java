package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Dados da nova alternativa")
public record AlternativaCriacaoRequest(
        @Schema(
                description = "Pergunta dona",
                example = "9f4a6c12-3e87-4d05-a2b9-5c1d7e8f0b36",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID perguntaId,

        @Schema(
                description = "Texto exibido",
                example = "Reunir a equipe em um único canal",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String texto,

        @Schema(
                description = "Verdadeiro move a marca da alternativa anterior",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean correta) {

    public AlternativaCriacaoRequest {
        if (texto != null) {
            texto = texto.trim();
        }
    }
}
