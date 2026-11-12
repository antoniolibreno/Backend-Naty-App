package com.projetointegrador.natysync.progresso.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Primeiro inicio registrado do video da atividade")
public record VideoIniciadoResponse(
        @Schema(description = "Atividade do video", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
        UUID atividadeId,

        @Schema(
                description = "Instante do primeiro inicio. Repetir o registro devolve o mesmo instante",
                example = "2026-11-11T14:29:10.123-03:00")
        OffsetDateTime videoIniciadoEm,

        @Schema(
                description = "Segundos que precisam decorrer desde o inicio para registrar a assistencia",
                example = "180")
        Integer duracaoSegundos) {}
