package com.projetointegrador.natysync.painel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de cadastro da empresa e do administrador inicial")
public record EmpresaCriacaoRequest(
        @Schema(
                description = "Nome da empresa",
                example = "Acme Treinamentos",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String nome,

        @Schema(
                description = "Desativar revoga as sessões dos integrantes",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean ativa,

        @Schema(
                description = "Fuso horário de região, define o dia de estudo",
                example = "America/Sao_Paulo",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 64)
        @NotBlank @Size(max = 64) String fusoHorario,

        @Schema(
                description = "CNPJ com ou sem máscara, numérico ou alfanumérico",
                example = "11.222.333/0001-81",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String cnpj,

        @Schema(
                description = "Razão social da empresa",
                example = "Acme Treinamentos Ltda",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String razaoSocial,

        @Schema(
                description = "Primeira conta ADMIN, criada junto com a empresa",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Valid AdministradorInicialRequest administrador) {

    public EmpresaRequest paraEmpresa() {
        return new EmpresaRequest(nome, ativa, fusoHorario, cnpj, razaoSocial);
    }

    @Schema(hidden = true)
    @AssertTrue(message = "fuso horario desconhecido") public boolean isFusoHorarioReconhecido() {
        return paraEmpresa().isFusoHorarioReconhecido();
    }

    @Schema(hidden = true)
    @AssertTrue(message = "cnpj invalido") public boolean isCnpjValido() {
        return paraEmpresa().isCnpjValido();
    }
}
