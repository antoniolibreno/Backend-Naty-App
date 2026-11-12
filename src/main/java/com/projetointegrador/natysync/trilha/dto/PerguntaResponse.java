package com.projetointegrador.natysync.trilha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Pergunta com alternativas")
public record PerguntaResponse(
        @Schema(description = "Identificador da pergunta", example = "a6f03d92-7b4e-4c15-8e2a-9d1c5b3f7a68")
        UUID id,

        @Schema(description = "Enunciado da pergunta", example = "Qual é a melhor prática ao iniciar um atendimento?")
        String enunciado,

        @Schema(description = "Posição no quiz, a partir de 1", example = "1")
        Integer ordem,

        @Schema(description = "Alternativas em ordem crescente")
        List<AlternativaResponse> alternativas) {}
