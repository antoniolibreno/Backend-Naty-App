package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Schema(description = "Ordem desejada dos itens")
public record OrdemRequest(
        @Schema(
                description = "Todos os identificadores, na ordem final",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "[\"5d1f8a63-7e2c-4b90-a4d5-1f3b9c6e8a07\"]")
        @NotEmpty List<UUID> ids) {}
