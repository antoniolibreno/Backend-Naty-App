package com.projetointegrador.natysync.progresso.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Trilha com o estado de cada atividade para o integrante")
public record TrilhaProgressoResponse(
        @Schema(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
        UUID id,

        @Schema(description = "Titulo da trilha", example = "Atendimento pelo WhatsApp")
        String titulo,

        @Schema(description = "Descricao da trilha", example = "Fundamentos do atendimento ao cliente com a Naty")
        String descricao,

        @Schema(description = "Posicao da trilha entre as ativas, a partir de 1", example = "1")
        Integer ordem,

        @Schema(description = "Total de atividades da trilha", example = "6")
        int totalAtividades,

        @Schema(description = "Atividades da trilha concluidas pelo integrante", example = "2")
        int atividadesConcluidas,

        @Schema(description = "Percentual concluido, de 0 a 100, arredondado para baixo", example = "33")
        int percentualConcluido,

        @Schema(
                description = "Primeira atividade nao concluida da trilha. Nulo quando tudo esta concluido",
                example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55",
                nullable = true)
        UUID proximaAtividadeId,

        @Schema(description = "Modulos da trilha em ordem crescente, com o estado de cada atividade")
        List<ModuloProgressoResponse> modulos) {}
