package com.projetointegrador.natysync.gamificacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Conquista obtida pelo integrante")
public record ConquistaResponse(
        @Schema(description = "Código da conquista no catálogo", example = "PRIMEIRA_ATIVIDADE")
        String codigo,

        @Schema(description = "Nome para exibição", example = "Primeiro passo")
        String nome,

        @Schema(description = "Regra que concede a conquista", example = "Concluiu a primeira atividade da trilha")
        String descricao,

        @Schema(description = "Instante em que a conquista foi obtida", example = "2026-11-10T19:45:00-03:00")
        OffsetDateTime obtidaEm) {}
