package com.projetointegrador.natysync.progresso.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Resultado da correcao de uma tentativa de quiz, sem gabarito")
public record TentativaQuizResponse(
        @Schema(description = "Identificador da tentativa registrada", example = "7d2a9e63-4c1b-4f58-a3d0-5e8b1c6f2a94")
        UUID tentativaId,

        @Schema(description = "Atividade do quiz", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
        UUID atividadeId,

        @Schema(
                description = "Nota da tentativa, de 0 a 100: porcentagem de respostas corretas arredondada",
                example = "75")
        int nota,

        @Schema(description = "Indica se a nota atingiu a nota minima do quiz", example = "true")
        boolean aprovado,

        @Schema(description = "Instante em que a tentativa foi registrada", example = "2026-11-11T14:32:10.123-03:00")
        OffsetDateTime criadaEm,

        @Schema(
                description =
                        "Perguntas respondidas de forma errada. Nao informa a alternativa escolhida nem a correta")
        List<PerguntaErrada> perguntasErradas,

        @Schema(
                description = "Instante da primeira conclusao da atividade. Nulo se nenhuma tentativa foi aprovada",
                example = "2026-11-11T14:32:10.123-03:00",
                nullable = true)
        OffsetDateTime concluidoEm,

        @Schema(
                description = "Primeira atividade nao concluida da trilha. Nulo quando tudo esta concluido",
                example = "f4c1d8a7-6b3e-4a92-9e05-3b7d1a5c8e46",
                nullable = true)
        UUID proximaAtividadeId) {

    @Schema(description = "Pergunta respondida de forma errada")
    public record PerguntaErrada(
            @Schema(description = "Identificador da pergunta", example = "a6f03d92-7b4e-4c15-8e2a-9d1c5b3f7a68")
            UUID perguntaId) {}
}
