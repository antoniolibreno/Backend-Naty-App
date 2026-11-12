package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QuizAlteracaoRequest(
        @NotNull @Min(0) @Max(100) Integer notaMinima) {}
