package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Alternativa com gabarito")
public record AlternativaAdminResponse(
        @Schema(description = "Identificador da alternativa", example = "6e0b3d95-8a21-4c7f-9d14-2f5a8c1e7b40")
        UUID id,

        @Schema(description = "Texto exibido", example = "Reunir a equipe em um único canal")
        String texto,

        @Schema(description = "Marca de gabarito", example = "true")
        boolean correta,

        @Schema(description = "Posição, a partir de 1", example = "1")
        Integer ordem) {}
