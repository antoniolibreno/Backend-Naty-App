package com.projetointegrador.natysync.empresa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(@NotBlank @Size(max = 255) String nome, @NotNull Boolean ativa) {
    public EmpresaRequest {
        if (nome != null) {
            nome = nome.trim();
        }
    }
}
