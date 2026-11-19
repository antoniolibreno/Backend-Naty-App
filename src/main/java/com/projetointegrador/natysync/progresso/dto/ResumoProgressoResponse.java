package com.projetointegrador.natysync.progresso.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Resumo do progresso nas trilhas ativas")
public record ResumoProgressoResponse(
        @Schema(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
        UUID integranteId,

        @Schema(description = "Total de atividades das trilhas ativas", example = "6")
        int totalAtividades,

        @Schema(description = "Atividades concluídas", example = "2")
        int atividadesConcluidas,

        @Schema(description = "Percentual concluído, de 0 a 100", example = "33")
        int percentualConcluido,

        @Schema(description = "Primeira atividade não concluída. Nulo se tudo concluído", nullable = true)
        ProximaAtividadeResponse proximaAtividade) {}
