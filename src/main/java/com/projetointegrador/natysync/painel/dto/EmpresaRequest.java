package com.projetointegrador.natysync.painel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DateTimeException;
import java.time.ZoneId;

@Schema(description = "Dados de cadastro de uma empresa")
public record EmpresaRequest(
        @Schema(
                description = "Nome da empresa",
                example = "Acme Treinamentos",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String nome,

        @Schema(
                description = "Indica se a empresa esta ativa. Desativar revoga as sessoes dos integrantes",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean ativa,

        @Schema(
                description = "Identificador de fuso horario de regiao, usado no dia de estudo da empresa",
                example = "America/Sao_Paulo",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 64)
        @NotBlank @Size(max = 64) String fusoHorario) {

    public EmpresaRequest {
        if (nome != null) {
            nome = nome.trim();
        }
        if (fusoHorario != null) {
            fusoHorario = fusoHorario.trim();
        }
    }

    @Schema(hidden = true)
    @AssertTrue(message = "fuso horario desconhecido") public boolean isFusoHorarioReconhecido() {
        if (fusoHorario == null || fusoHorario.isEmpty()) {
            return true;
        }
        try {
            ZoneId.of(fusoHorario);
            return true;
        } catch (DateTimeException excecao) {
            return false;
        }
    }

    public ZoneId fuso() {
        return ZoneId.of(fusoHorario);
    }
}
