package com.projetointegrador.natysync.trilha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Alternativa de uma pergunta")
public record AlternativaResponse(
        @Schema(description = "Identificador da alternativa", example = "1e7b9c34-5d2a-4a86-b0f3-6c8d2e4a9b71")
        UUID id,

        @Schema(description = "Texto da alternativa", example = "Responder o cliente dentro do horário combinado")
        String texto,

        @Schema(description = "Posição da alternativa na pergunta, a partir de 1", example = "1")
        Integer ordem) {}
