package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Alteracao de titulo e descricao do modulo")
public record ModuloAlteracaoRequest(
        @Schema(
                description = "Titulo, sem espacos nas pontas, ate 255 caracteres",
                example = "Fundamentos da plataforma",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String titulo,

        @Schema(description = "Descricao livre do modulo", example = "Conceitos iniciais e primeiros passos")
        String descricao) {

    public ModuloAlteracaoRequest {
        if (titulo != null) {
            titulo = titulo.trim();
        }
    }
}
