package com.projetointegrador.natysync.trilha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlternativaEscritaRequest(
        @NotBlank String texto,
        @NotNull Boolean correta,
        @NotNull Integer ordem) {}
