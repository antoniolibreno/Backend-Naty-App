package com.projetointegrador.natysync.acompanhamento.dto;

import com.projetointegrador.natysync.acompanhamento.SituacaoDeAcompanhamento;
import com.projetointegrador.natysync.usuario.Papel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(
        description = "Integrante com a situacao de progresso, calculada sobre as trilhas ativas."
                + " Conclusao de atividade de trilha inativa nao entra na conta")
public record IntegranteAcompanhadoResponse(
        @Schema(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
        UUID integranteId,

        @Schema(description = "Nome do integrante", example = "Ana Souza")
        String nome,

        @Schema(description = "E-mail do integrante", example = "ana.souza@acme.com.br")
        String email,

        @Schema(description = "Papel do integrante na empresa", example = "INTEGRANTE")
        Papel papel,

        @Schema(
                description = "Situacao do integrante. CONCLUIU quando concluiu todas as atividades ativas."
                        + " PARADO quando os dias sem concluir alcancam o prazo configurado em"
                        + " app.acompanhamento.dias-parado (padrao 3). SEM_INICIO para conta nova sem conclusao."
                        + " AVANCANDO nos demais casos. Quem concluiu tudo nunca esta parado",
                example = "AVANCANDO")
        SituacaoDeAcompanhamento situacao,

        @Schema(description = "Atividades ativas concluidas", example = "6")
        int atividadesConcluidas,

        @Schema(description = "Total de atividades das trilhas ativas", example = "20")
        int totalAtividades,

        @Schema(
                description = "Concluidas sobre total, em percentual inteiro arredondado para baixo."
                        + " Zero quando nao ha atividade",
                example = "30")
        int percentualConcluido,

        @Schema(
                description = "Primeira atividade ativa nao concluida. Nulo quando o integrante concluiu tudo",
                nullable = true)
        AtividadeAtualResponse atividadeAtual,

        @Schema(
                description = "Instante da conclusao mais recente em atividade ativa. Nulo sem conclusao",
                example = "2026-11-10T19:45:00-03:00",
                nullable = true)
        OffsetDateTime ultimaConclusaoEm,

        @Schema(
                description = "Dias de calendario no fuso da empresa desde a ultima conclusao ou,"
                        + " sem conclusao, desde a criacao da conta",
                example = "1")
        long diasSemConcluir) {}
