package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlternativaRequest(
        @NotBlank String texto, @NotNull Boolean correta) {

    public AlternativaRequest {
        if (texto != null) {
            texto = texto.trim();
        }
    }
}
