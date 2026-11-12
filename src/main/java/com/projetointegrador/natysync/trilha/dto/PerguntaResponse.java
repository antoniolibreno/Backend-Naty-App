package com.projetointegrador.natysync.trilha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Pergunta de um quiz com suas alternativas")
public record PerguntaResponse(
        @Schema(
                description = "Identificador da pergunta, enviado na tentativa de quiz",
                example = "a6f03d92-7b4e-4c15-8e2a-9d1c5b3f7a68")
        UUID id,

        @Schema(description = "Enunciado da pergunta", example = "Qual e a melhor pratica ao iniciar um atendimento?")
        String enunciado,

        @Schema(description = "Posicao da pergunta no quiz, a partir de 1", example = "1")
        Integer ordem,

        @Schema(description = "Alternativas da pergunta em ordem crescente")
        List<AlternativaResponse> alternativas) {}
