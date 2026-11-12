package com.projetointegrador.natysync.progresso.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Trilha com o estado das atividades")
public record TrilhaProgressoResponse(
        @Schema(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
        UUID id,

        @Schema(description = "Título da trilha", example = "Atendimento pelo WhatsApp")
        String titulo,

        @Schema(description = "Descrição da trilha", example = "Fundamentos do atendimento ao cliente com a Naty")
        String descricao,

        @Schema(description = "Posição entre as ativas, a partir de 1", example = "1")
        Integer ordem,

        @Schema(description = "Total de atividades da trilha", example = "6")
        int totalAtividades,

        @Schema(description = "Atividades concluídas", example = "2")
        int atividadesConcluidas,

        @Schema(description = "Percentual concluído, de 0 a 100", example = "33")
        int percentualConcluido,

        @Schema(
                description = "Próxima atividade. Nulo se tudo concluído",
                example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55",
                nullable = true)
        UUID proximaAtividadeId,

        @Schema(description = "Módulos com o estado das atividades")
        List<ModuloProgressoResponse> modulos) {}
