package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Dados do novo módulo")
public record ModuloCriacaoRequest(
        @Schema(
                description = "Trilha dona",
                example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID trilhaId,

        @Schema(
                description = "Título, até 255 caracteres",
                example = "Fundamentos da plataforma",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String titulo,

        @Schema(description = "Descrição", example = "Conceitos iniciais e primeiros passos")
        String descricao) {

    public ModuloCriacaoRequest {
        if (titulo != null) {
            titulo = titulo.trim();
        }
    }
}
