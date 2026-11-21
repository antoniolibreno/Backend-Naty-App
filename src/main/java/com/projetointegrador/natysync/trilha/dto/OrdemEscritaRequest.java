package com.projetointegrador.natysync.trilha.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record OrdemEscritaRequest(
        @NotNull UUID paiId, @NotEmpty List<UUID> ids) {}
