package com.projetointegrador.natysync.acompanhamento.dto;

import com.projetointegrador.natysync.acompanhamento.SituacaoDeAcompanhamento;
import com.projetointegrador.natysync.usuario.Papel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Integrante com a situação de progresso")
public record IntegranteAcompanhadoResponse(
        @Schema(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
        UUID integranteId,

        @Schema(description = "Nome do integrante", example = "Ana Souza")
        String nome,

        @Schema(description = "E-mail do integrante", example = "ana.souza@acme.com.br")
        String email,

        @Schema(description = "Papel do integrante na empresa", example = "INTEGRANTE")
        Papel papel,

        @Schema(description = "Situação: CONCLUIU, PARADO, SEM_INICIO ou AVANCANDO", example = "AVANCANDO")
        SituacaoDeAcompanhamento situacao,

        @Schema(description = "Atividades concluídas", example = "6")
        int atividadesConcluidas,

        @Schema(description = "Total de atividades das trilhas ativas", example = "20")
        int totalAtividades,

        @Schema(description = "Percentual de atividades concluídas", example = "30")
        int percentualConcluido,

        @Schema(description = "Próxima atividade. Nulo se concluiu tudo", nullable = true)
        AtividadeAtualResponse atividadeAtual,

        @Schema(
                description = "Instante da última conclusão. Nulo sem conclusão",
                example = "2026-11-10T19:45:00-03:00",
                nullable = true)
        OffsetDateTime ultimaConclusaoEm,

        @Schema(description = "Dias desde a última conclusão", example = "1")
        long diasSemConcluir) {}
