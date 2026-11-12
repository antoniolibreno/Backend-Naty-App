package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Schema(
        description =
                "Criacao do quiz de uma atividade com perguntas e alternativas. Cada atividade tem no maximo um quiz")
public record QuizCriacaoRequest(
        @Schema(
                description =
                        "Atividade que recebe o quiz. Com quiz, a atividade conclui por aprovacao e nao por video",
                example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID atividadeId,

        @Schema(
                description =
                        "Nota minima percentual, de 0 a 100, para aprovar o quiz. Aprovar conclui a atividade. Vale 70 quando omitida",
                example = "70",
                minimum = "0",
                maximum = "100")
        @Min(0) @Max(100) Integer notaMinima,

        @Schema(
                description = "Perguntas do quiz, ao menos uma, na ordem de exibicao",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<@Valid PerguntaRequest> perguntas) {

    private static final int NOTA_MINIMA_PADRAO = 70;

    public QuizCriacaoRequest {
        if (notaMinima == null) {
            notaMinima = NOTA_MINIMA_PADRAO;
        }
    }
}
