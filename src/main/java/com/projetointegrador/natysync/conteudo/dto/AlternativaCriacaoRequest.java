package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Criacao de alternativa. Entra no fim das alternativas da pergunta")
public record AlternativaCriacaoRequest(
        @Schema(
                description = "Pergunta que recebe a alternativa",
                example = "9f4a6c12-3e87-4d05-a2b9-5c1d7e8f0b36",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID perguntaId,

        @Schema(
                description = "Texto da alternativa, sem espacos nas pontas",
                example = "Reunir a equipe em um unico canal",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String texto,

        @Schema(
                description =
                        "Marca de gabarito. Marcar verdadeiro move a marca da alternativa anterior na mesma transacao. Desmarcar a unica correta responde 409 GABARITO_INVALIDO",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean correta) {

    public AlternativaCriacaoRequest {
        if (texto != null) {
            texto = texto.trim();
        }
    }
}
