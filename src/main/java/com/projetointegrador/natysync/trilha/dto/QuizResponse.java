package com.projetointegrador.natysync.trilha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Quiz de uma atividade, sem gabarito")
public record QuizResponse(
        @Schema(description = "Identificador do quiz", example = "4c8e2b75-1a9d-4f30-b6e7-2d5a8c1f3e90")
        UUID id,

        @Schema(description = "Atividade dona do quiz", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
        UUID atividadeId,

        @Schema(
                description = "Nota minima, de 0 a 100, para aprovar a tentativa e concluir a atividade",
                example = "70")
        Integer notaMinima,

        @Schema(description = "Perguntas do quiz em ordem crescente")
        List<PerguntaResponse> perguntas) {}
