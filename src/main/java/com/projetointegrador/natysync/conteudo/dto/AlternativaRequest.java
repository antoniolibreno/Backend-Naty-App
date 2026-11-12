package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Alternativa de uma pergunta, na criacao do quiz, da pergunta ou na alteracao da alternativa")
public record AlternativaRequest(
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

    public AlternativaRequest {
        if (texto != null) {
            texto = texto.trim();
        }
    }
}
