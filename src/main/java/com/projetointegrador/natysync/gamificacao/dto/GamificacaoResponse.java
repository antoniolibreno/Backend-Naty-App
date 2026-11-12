package com.projetointegrador.natysync.gamificacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(
        description = "Perfil de gamificacao do integrante. Pontos, sequencia e posicao sao calculados a cada leitura"
                + " a partir dos fatos de estudo")
public record GamificacaoResponse(
        @Schema(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
        UUID integranteId,

        @Schema(description = "Nome do integrante", example = "Ana Souza")
        String nome,

        @Schema(
                description = "Soma do xp das atividades concluidas. Cada atividade concede seu xp uma unica vez,"
                        + " na primeira conclusao, com o valor vigente naquele momento",
                example = "350")
        long pontos,

        @Schema(
                description = "Posicao ordinal entre as contas ativas da empresa. Ordem por pontos decrescentes,"
                        + " depois quem chegou primeiro ao total, depois nome. A posicao nao se repete",
                example = "2")
        long posicaoNoRanking,

        @Schema(description = "Sequencia de dias de estudo do integrante")
        SequenciaResponse sequencia,

        @Schema(description = "Conquistas obtidas pelo integrante")
        List<ConquistaResponse> conquistas) {}
