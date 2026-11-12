package com.projetointegrador.natysync.painel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Empresa cliente da Naty")
public record EmpresaResponse(
        @Schema(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
        UUID id,

        @Schema(description = "Nome da empresa", example = "Acme Treinamentos")
        String nome,

        @Schema(description = "Indica se a empresa está ativa", example = "true")
        boolean ativa,

        @Schema(description = "Fuso horário da empresa", example = "America/Sao_Paulo")
        String fusoHorario,

        @Schema(
                description = "CNPJ com 14 caracteres, nulo se não informado",
                example = "11222333000181",
                nullable = true)
        String cnpj,

        @Schema(description = "Razão social", example = "Acme Treinamentos Ltda", nullable = true)
        String razaoSocial,

        @Schema(description = "Instante de criação", example = "2026-11-11T14:32:10.123-03:00")
        OffsetDateTime criadoEm,

        @Schema(description = "Instante da última alteração", example = "2026-11-11T15:10:00.000-03:00")
        OffsetDateTime atualizadoEm) {}
