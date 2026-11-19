package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados da nova pergunta")
public record PerguntaCriacaoRequest(
        @Schema(
                description = "Quiz dono",
                example = "3b8e2d71-9c40-4a65-b1f8-7d5e0a2c4f93",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID quizId,

        @Schema(
                description = "Enunciado",
                example = "Qual é a melhor forma de organizar o atendimento?",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String enunciado,

        @Schema(description = "Ao menos duas, exatamente uma correta") @Size(min = 2) List<@Valid AlternativaRequest> alternativas) {

    public PerguntaCriacaoRequest {
        if (enunciado != null) {
            enunciado = enunciado.trim();
        }
    }

    @Schema(hidden = true)
    @AssertTrue(message = "a pergunta deve ter exatamente uma alternativa correta") public boolean isUmaAlternativaCorreta() {
        return RegraDeGabarito.exatamenteUmaCorreta(alternativas);
    }
}
