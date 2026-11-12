package com.projetointegrador.natysync.painel.dto;

import com.projetointegrador.natysync.painel.Cnpj;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DateTimeException;
import java.time.ZoneId;

@Schema(description = "Dados de cadastro da empresa")
public record EmpresaRequest(
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
        @NotBlank @Size(max = 255) String razaoSocial) {

    public EmpresaRequest {
        if (nome != null) {
            nome = nome.trim();
        }
        if (fusoHorario != null) {
            fusoHorario = fusoHorario.trim();
        }
        cnpj = Cnpj.normalizar(cnpj);
        if (razaoSocial != null) {
            razaoSocial = razaoSocial.trim();
        }
    }

    @Schema(hidden = true)
    @AssertTrue(message = "cnpj invalido") public boolean isCnpjValido() {
        return cnpj == null || cnpj.isEmpty() || Cnpj.valido(cnpj);
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
