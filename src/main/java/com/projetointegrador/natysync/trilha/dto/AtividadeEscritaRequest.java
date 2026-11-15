package com.projetointegrador.natysync.trilha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AtividadeEscritaRequest(
        @NotNull UUID moduloId,
        @NotBlank String titulo,
        String descricao,
        @NotNull Integer ordem,
        String imagemUrl,
        String videoUrl,
        Integer duracaoSegundos,
        @NotNull Integer xp) {}
