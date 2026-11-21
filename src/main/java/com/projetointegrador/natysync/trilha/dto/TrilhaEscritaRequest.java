package com.projetointegrador.natysync.trilha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrilhaEscritaRequest(
        @NotBlank String titulo, String descricao, @NotNull Integer ordem) {}
