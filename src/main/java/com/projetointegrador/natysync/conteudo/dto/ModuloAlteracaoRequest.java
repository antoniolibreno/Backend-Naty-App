package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModuloAlteracaoRequest(
        @NotBlank @Size(max = 255) String titulo, String descricao) {

    public ModuloAlteracaoRequest {
        if (titulo != null) {
            titulo = titulo.trim();
        }
    }
}
