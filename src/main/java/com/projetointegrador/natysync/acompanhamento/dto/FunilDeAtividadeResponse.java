package com.projetointegrador.natysync.acompanhamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Funil de uma atividade ativa")
public record FunilDeAtividadeResponse(
        @Schema(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
        UUID atividadeId,

        @Schema(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
        UUID trilhaId,

        @Schema(description = "Título da atividade", example = "Como cadastrar um contato")
        String titulo,

        @Schema(description = "Integrantes com a atividade liberada", example = "40")
        long chegaram,

        @Schema(description = "Integrantes que concluíram a atividade", example = "28")
        long passaram,

        @Schema(description = "Percentual de quem passou entre quem chegou", example = "70")
        int percentualDePassagem) {}
