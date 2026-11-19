package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Novo enunciado da pergunta")
public record PerguntaAlteracaoRequest(
        @Schema(
                description = "Enunciado",
                example = "Qual é a melhor forma de organizar o atendimento?",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String enunciado) {

    public PerguntaAlteracaoRequest {
        if (enunciado != null) {
            enunciado = enunciado.trim();
        }
    }
}
