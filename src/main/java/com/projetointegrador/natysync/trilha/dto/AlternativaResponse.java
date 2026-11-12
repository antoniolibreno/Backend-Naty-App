package com.projetointegrador.natysync.trilha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Alternativa de uma pergunta de quiz. Nao informa se e a correta")
public record AlternativaResponse(
        @Schema(
                description = "Identificador da alternativa, enviado na tentativa de quiz",
                example = "1e7b9c34-5d2a-4a86-b0f3-6c8d2e4a9b71")
        UUID id,

        @Schema(description = "Texto da alternativa", example = "Responder o cliente dentro do horario combinado")
        String texto,

        @Schema(description = "Posicao da alternativa na pergunta, a partir de 1", example = "1")
        Integer ordem) {}
