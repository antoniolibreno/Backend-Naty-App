package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados de uma alternativa")
public record AlternativaRequest(
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

    public AlternativaRequest {
        if (texto != null) {
            texto = texto.trim();
        }
    }
}
