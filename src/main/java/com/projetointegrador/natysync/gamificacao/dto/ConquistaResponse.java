package com.projetointegrador.natysync.gamificacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Conquista obtida pelo integrante. A conquista sai uma unica vez por integrante e nao e retirada")
public record ConquistaResponse(
        @Schema(description = "Codigo estavel da conquista no catalogo global", example = "PRIMEIRA_ATIVIDADE")
        String codigo,

        @Schema(description = "Nome da conquista para exibicao", example = "Primeiro passo")
        String nome,

        @Schema(description = "Regra que concede a conquista", example = "Concluiu a primeira atividade da trilha")
        String descricao,

        @Schema(
                description = "Instante do fato de estudo que atingiu o limiar da conquista",
                example = "2026-11-10T19:45:00-03:00")
        OffsetDateTime obtidaEm) {}
