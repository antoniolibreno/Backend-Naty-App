package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrilhaRequest(@NotBlank @Size(max = 255) String titulo, String descricao) {

    public TrilhaRequest {
        if (titulo != null) {
            titulo = titulo.trim();
        }
    }
}
