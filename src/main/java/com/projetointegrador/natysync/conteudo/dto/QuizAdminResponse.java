package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Quiz com gabarito")
public record QuizAdminResponse(
        @Schema(description = "Identificador do quiz", example = "3b8e2d71-9c40-4a65-b1f8-7d5e0a2c4f93")
        UUID id,

        @Schema(description = "Atividade dona", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
        UUID atividadeId,

        @Schema(description = "Nota mínima para aprovar, de 0 a 100", example = "70")
        Integer notaMinima,

        @Schema(description = "Perguntas") List<PerguntaAdminResponse> perguntas) {}
