package com.projetointegrador.natysync.progresso.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Schema(description = "Respostas de uma tentativa de quiz. A nota e calculada pelo servidor")
public record TentativaQuizRequest(
        @Schema(
                description = "Uma resposta para cada pergunta do quiz, sem repetir pergunta",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<@Valid Resposta> respostas) {

    @Schema(description = "Alternativa escolhida para uma pergunta")
    public record Resposta(
            @Schema(
                    description = "Pergunta do quiz da atividade",
                    example = "a6f03d92-7b4e-4c15-8e2a-9d1c5b3f7a68",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull UUID perguntaId,

            @Schema(
                    description = "Alternativa escolhida, que precisa pertencer a pergunta",
                    example = "1e7b9c34-5d2a-4a86-b0f3-6c8d2e4a9b71",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull UUID alternativaId) {}
}
