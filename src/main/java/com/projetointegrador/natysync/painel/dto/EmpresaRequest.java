package com.projetointegrador.natysync.painel.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DateTimeException;
import java.time.ZoneId;

public record EmpresaRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotNull Boolean ativa,
        @NotBlank @Size(max = 64) String fusoHorario) {

    public EmpresaRequest {
        if (nome != null) {
            nome = nome.trim();
        }
        if (fusoHorario != null) {
            fusoHorario = fusoHorario.trim();
        }
    }

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
