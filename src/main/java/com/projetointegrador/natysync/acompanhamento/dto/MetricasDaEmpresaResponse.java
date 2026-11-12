package com.projetointegrador.natysync.acompanhamento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
        description = "Metricas da empresa sobre integrantes ativos de papel diferente de NATY e trilhas ativas."
                + " As contagens por situacao coincidem com a listagem de integrantes")
public record MetricasDaEmpresaResponse(
        @Schema(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
        UUID empresaId,

        @Schema(description = "Total de integrantes considerados", example = "25")
        int totalIntegrantes,

        @Schema(
                description = "Percentual de integrantes com ao menos uma atividade concluida,"
                        + " inteiro arredondado para baixo. Zero sem integrantes",
                example = "80")
        int adesaoPercentual,

        @Schema(
                description = "Media do percentual concluido de cada integrante, inteira e arredondada para baixo."
                        + " Zero sem integrantes",
                example = "45")
        int percentualMedioDeConclusao,

        @Schema(description = "Integrantes na situacao CONCLUIU", example = "5")
        int concluiram,

        @Schema(description = "Integrantes na situacao AVANCANDO", example = "12")
        int avancando,

        @Schema(description = "Integrantes na situacao PARADO", example = "6")
        int parados,

        @Schema(description = "Integrantes na situacao SEM_INICIO", example = "2")
        int semInicio) {}
