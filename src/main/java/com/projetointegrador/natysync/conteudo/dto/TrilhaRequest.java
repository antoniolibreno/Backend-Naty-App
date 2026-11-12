package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Titulo e descricao da trilha, na criacao e na alteracao")
public record TrilhaRequest(
        @Schema(
                description = "Titulo, sem espacos nas pontas, ate 255 caracteres",
                example = "Onboarding Naty",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String titulo,

        @Schema(description = "Descricao livre da trilha", example = "Conceitos iniciais e primeiros passos")
        String descricao) {

    public TrilhaRequest {
        if (titulo != null) {
            titulo = titulo.trim();
        }
    }
}
