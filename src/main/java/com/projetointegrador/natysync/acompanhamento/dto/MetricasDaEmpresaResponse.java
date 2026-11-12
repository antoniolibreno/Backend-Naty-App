package com.projetointegrador.natysync.acompanhamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Métricas de progresso da empresa")
public record MetricasDaEmpresaResponse(
        @Schema(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
        UUID empresaId,

        @Schema(description = "Total de integrantes acompanhados", example = "25")
        int totalIntegrantes,

        @Schema(description = "Percentual com ao menos uma atividade concluída", example = "80")
        int adesaoPercentual,

        @Schema(description = "Média do percentual concluído dos integrantes", example = "45")
        int percentualMedioDeConclusao,

        @Schema(description = "Integrantes na situação CONCLUIU", example = "5")
        int concluiram,

        @Schema(description = "Integrantes na situação AVANCANDO", example = "12")
        int avancando,

        @Schema(description = "Integrantes na situação PARADO", example = "6")
        int parados,

        @Schema(description = "Integrantes na situação SEM_INICIO", example = "2")
        int semInicio) {}
