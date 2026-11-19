package com.projetointegrador.natysync.gamificacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Perfil de gamificação do integrante")
public record GamificacaoResponse(
        @Schema(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
        UUID integranteId,

        @Schema(description = "Nome do integrante", example = "Ana Souza")
        String nome,

        @Schema(description = "Soma do xp das atividades concluídas", example = "350")
        long pontos,

        @Schema(description = "Posição entre as contas ativas da empresa", example = "2")
        long posicaoNoRanking,

        @Schema(description = "Sequência de dias de estudo") SequenciaResponse sequencia,

        @Schema(description = "Conquistas obtidas pelo integrante")
        List<ConquistaResponse> conquistas) {}
