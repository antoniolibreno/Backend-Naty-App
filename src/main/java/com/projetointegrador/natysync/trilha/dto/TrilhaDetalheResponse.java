package com.projetointegrador.natysync.trilha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Trilha de treinamento com modulos e atividades")
public record TrilhaDetalheResponse(
        @Schema(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
        UUID id,

        @Schema(description = "Titulo da trilha", example = "Atendimento pelo WhatsApp")
        String titulo,

        @Schema(description = "Descricao da trilha", example = "Fundamentos do atendimento ao cliente com a Naty")
        String descricao,

        @Schema(description = "Posicao da trilha entre as ativas, a partir de 1", example = "1")
        Integer ordem,

        @Schema(description = "Modulos da trilha em ordem crescente")
        List<ModuloResponse> modulos) {}
