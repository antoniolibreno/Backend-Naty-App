package com.projetointegrador.natysync.trilha.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record QuizEscritaRequest(
        @NotNull UUID atividadeId,
        @NotNull @Min(0) @Max(100) Integer notaMinima) {}
