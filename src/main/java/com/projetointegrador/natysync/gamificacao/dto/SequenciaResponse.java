package com.projetointegrador.natysync.gamificacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sequência de dias de estudo, no fuso da empresa")
public record SequenciaResponse(
        @Schema(description = "Dias seguidos de estudo até o dia corrente ou o anterior", example = "4")
        int atual,

        @Schema(description = "Maior sequência já alcançada", example = "7")
        int maior,

        @Schema(description = "Verdadeiro se já estudou no dia corrente", example = "true")
        boolean diaCorrenteEstudado) {}
