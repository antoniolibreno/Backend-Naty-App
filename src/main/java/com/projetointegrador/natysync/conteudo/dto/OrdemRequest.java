package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Schema(
        description =
                "Ordem desejada dos irmaos. A lista deve ter todos os irmaos atuais, sem repeticao, senao responde 409 ORDEM_INVALIDA")
public record OrdemRequest(
        @Schema(
                description =
                        "Identificadores de todos os irmaos na ordem final. A posicao na lista vira a ordem de 1 a n",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "[\"5d1f8a63-7e2c-4b90-a4d5-1f3b9c6e8a07\"]")
        @NotEmpty List<UUID> ids) {}
