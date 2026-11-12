package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Alternativa na leitura administrativa. Unico tipo de alternativa que traz o campo correta")
public record AlternativaAdminResponse(
        @Schema(description = "Identificador da alternativa", example = "6e0b3d95-8a21-4c7f-9d14-2f5a8c1e7b40")
        UUID id,

        @Schema(description = "Texto da alternativa", example = "Reunir a equipe em um unico canal")
        String texto,

        @Schema(
                description = "Marca de gabarito. Cada pergunta tem exatamente uma alternativa correta",
                example = "true")
        boolean correta,

        @Schema(description = "Posicao entre as alternativas da pergunta, a partir de 1", example = "1")
        Integer ordem) {}
