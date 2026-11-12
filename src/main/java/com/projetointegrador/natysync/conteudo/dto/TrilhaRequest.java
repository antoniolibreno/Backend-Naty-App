package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Título e descrição da trilha")
public record TrilhaRequest(
        @Schema(
                description = "Título, até 255 caracteres",
                example = "Onboarding Naty",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String titulo,

        @Schema(description = "Descrição", example = "Conceitos iniciais e primeiros passos")
        String descricao) {

    public TrilhaRequest {
        if (titulo != null) {
            titulo = titulo.trim();
        }
    }
}
