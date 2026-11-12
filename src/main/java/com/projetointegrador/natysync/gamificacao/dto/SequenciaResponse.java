package com.projetointegrador.natysync.gamificacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Sequencia de dias de estudo. Os dias sao datas locais no fuso da empresa do integrante."
                + " Conta como estudo tentativa de quiz, primeiro video assistido e primeira conclusao de atividade")
public record SequenciaResponse(
        @Schema(
                description = "Dias seguidos de estudo terminando na data corrente ou na anterior,"
                        + " para a sequencia nao zerar antes de o dia acabar",
                example = "4")
        int atual,

        @Schema(description = "Maior sequencia de dias seguidos ja alcancada pelo integrante", example = "7")
        int maior,

        @Schema(
                description = "Verdadeiro quando ha estudo registrado na data corrente do fuso da empresa",
                example = "true")
        boolean diaCorrenteEstudado) {}
