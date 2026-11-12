package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Alteracao do enunciado da pergunta. As alternativas mudam pelas rotas de alternativa")
public record PerguntaAlteracaoRequest(
        @Schema(
                description = "Enunciado da pergunta, sem espacos nas pontas",
                example = "Qual e a melhor forma de organizar o atendimento?",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String enunciado) {

    public PerguntaAlteracaoRequest {
        if (enunciado != null) {
            enunciado = enunciado.trim();
        }
    }
}
