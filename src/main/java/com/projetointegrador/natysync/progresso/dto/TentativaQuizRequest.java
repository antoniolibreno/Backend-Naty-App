package com.projetointegrador.natysync.progresso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record TentativaQuizRequest(@NotEmpty List<@Valid Resposta> respostas) {

    public record Resposta(@NotNull UUID perguntaId, @NotNull UUID alternativaId) {}
}
