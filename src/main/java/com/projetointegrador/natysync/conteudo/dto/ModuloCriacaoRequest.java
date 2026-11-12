package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ModuloCriacaoRequest(
        @NotNull UUID trilhaId, @NotBlank @Size(max = 255) String titulo, String descricao) {

    public ModuloCriacaoRequest {
        if (titulo != null) {
            titulo = titulo.trim();
        }
    }
}
