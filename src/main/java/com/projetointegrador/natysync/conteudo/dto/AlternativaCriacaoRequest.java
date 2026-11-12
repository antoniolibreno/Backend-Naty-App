package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AlternativaCriacaoRequest(
        @NotNull UUID perguntaId,
        @NotBlank String texto,
        @NotNull Boolean correta) {

    public AlternativaCriacaoRequest {
        if (texto != null) {
            texto = texto.trim();
        }
    }
}
