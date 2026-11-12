package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Alteracao da nota minima do quiz. Tentativa antiga mantem a nota gravada")
public record QuizAlteracaoRequest(
        @Schema(
                description = "Nota minima percentual, de 0 a 100, para aprovar o quiz. Aprovar conclui a atividade",
                example = "70",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0",
                maximum = "100")
        @NotNull @Min(0) @Max(100) Integer notaMinima) {}
