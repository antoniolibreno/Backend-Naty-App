package com.projetointegrador.natysync.progresso.dto;

import com.projetointegrador.natysync.progresso.EstadoAtividade;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Resultado do registro de video assistido")
public record VideoAssistidoResponse(
        @Schema(description = "Atividade do video", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
        UUID atividadeId,

        @Schema(description = "Estado da atividade apos o registro", example = "CONCLUIDO")
        EstadoAtividade estado,

        @Schema(
                description = "Instante em que o video foi marcado como assistido",
                example = "2026-11-11T14:32:10.123-03:00")
        OffsetDateTime videoAssistidoEm,

        @Schema(
                description = "Instante da primeira conclusao. Nulo quando a atividade tem quiz e nao foi aprovada",
                example = "2026-11-11T14:32:10.123-03:00",
                nullable = true)
        OffsetDateTime concluidoEm,

        @Schema(
                description = "Primeira atividade nao concluida da trilha. Nulo quando tudo esta concluido",
                example = "f4c1d8a7-6b3e-4a92-9e05-3b7d1a5c8e46",
                nullable = true)
        UUID proximaAtividadeId) {}
