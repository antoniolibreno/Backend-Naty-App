package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Pergunta na leitura administrativa, com o gabarito de cada alternativa")
public record PerguntaAdminResponse(
        @Schema(description = "Identificador da pergunta", example = "9f4a6c12-3e87-4d05-a2b9-5c1d7e8f0b36")
        UUID id,

        @Schema(description = "Texto da pergunta", example = "Qual e a melhor forma de organizar o atendimento?")
        String enunciado,

        @Schema(description = "Posicao entre as perguntas do quiz, a partir de 1", example = "1")
        Integer ordem,

        @Schema(description = "Alternativas na ordem de exibicao, com a marca correta")
        List<AlternativaAdminResponse> alternativas) {}
