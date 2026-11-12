package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record QuizCriacaoRequest(
        @NotNull UUID atividadeId,
        @Min(0) @Max(100) Integer notaMinima,
        @NotEmpty List<@Valid PerguntaRequest> perguntas) {

    private static final int NOTA_MINIMA_PADRAO = 70;

    public QuizCriacaoRequest {
        if (notaMinima == null) {
            notaMinima = NOTA_MINIMA_PADRAO;
        }
    }
}
