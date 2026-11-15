package com.projetointegrador.natysync.trilha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ModuloEscritaRequest(
        @NotNull UUID trilhaId,
        @NotBlank String titulo,
        String descricao,
        @NotNull Integer ordem) {}
