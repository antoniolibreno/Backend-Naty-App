package com.projetointegrador.natysync.progresso.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Resultado da tentativa, sem gabarito")
public record TentativaQuizResponse(
        @Schema(description = "Identificador da tentativa", example = "7d2a9e63-4c1b-4f58-a3d0-5e8b1c6f2a94")
        UUID tentativaId,

        @Schema(description = "Atividade do quiz", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
        UUID atividadeId,

        @Schema(description = "Nota de 0 a 100", example = "75")
        int nota,

        @Schema(description = "Indica se atingiu a nota mínima", example = "true")
        boolean aprovado,

        @Schema(description = "Instante da tentativa", example = "2026-11-11T14:32:10.123-03:00")
        OffsetDateTime criadaEm,

        @Schema(description = "Perguntas respondidas errado")
        List<PerguntaErrada> perguntasErradas,

        @Schema(
                description = "Instante da conclusão. Nulo sem aprovação",
                example = "2026-11-11T14:32:10.123-03:00",
                nullable = true)
        OffsetDateTime concluidoEm,

        @Schema(
                description = "Próxima atividade. Nulo se tudo concluído",
                example = "f4c1d8a7-6b3e-4a92-9e05-3b7d1a5c8e46",
                nullable = true)
        UUID proximaAtividadeId) {

    @Schema(description = "Pergunta respondida errado")
    public record PerguntaErrada(
            @Schema(description = "Identificador da pergunta", example = "a6f03d92-7b4e-4c15-8e2a-9d1c5b3f7a68")
            UUID perguntaId) {}
}
