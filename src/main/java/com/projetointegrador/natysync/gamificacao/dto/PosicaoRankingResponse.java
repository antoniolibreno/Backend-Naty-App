package com.projetointegrador.natysync.gamificacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
        description =
                "Linha do ranking da empresa. Entram todas as contas ativas, de qualquer papel e inclusive sem ponto")
public record PosicaoRankingResponse(
        @Schema(
                description = "Posicao ordinal na classificacao completa da empresa, a primeira e 1."
                        + " Nao se repete entre contas",
                example = "1")
        long posicao,

        @Schema(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
        UUID integranteId,

        @Schema(description = "Nome do integrante", example = "Ana Souza")
        String nome,

        @Schema(description = "Soma do xp das atividades concluidas pelo integrante", example = "350")
        long pontos) {}
