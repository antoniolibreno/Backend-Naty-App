package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Pergunta enviada na criacao do quiz, com suas alternativas")
public record PerguntaRequest(
        @Schema(
                description = "Enunciado da pergunta, sem espacos nas pontas",
                example = "Qual e a melhor forma de organizar o atendimento?",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String enunciado,

        @Schema(
                description =
                        "Alternativas da pergunta. Ao menos duas e exatamente uma com correta verdadeiro, senao responde 400 FALHA_DE_VALIDACAO")
        @Size(min = 2) List<@Valid AlternativaRequest> alternativas) {

    public PerguntaRequest {
        if (enunciado != null) {
            enunciado = enunciado.trim();
        }
    }

    @Schema(hidden = true)
    @AssertTrue(message = "a pergunta deve ter exatamente uma alternativa correta") public boolean isUmaAlternativaCorreta() {
        return RegraDeGabarito.exatamenteUmaCorreta(alternativas);
    }
}
