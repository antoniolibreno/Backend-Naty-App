package com.projetointegrador.natysync.conteudo.dto;

import com.projetointegrador.natysync.conteudo.AcaoDeConteudo;
import com.projetointegrador.natysync.conteudo.EntidadeDeConteudo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Registro de auditoria de uma alteracao de conteudo. Sobrevive a remocao do item alterado")
public record AlteracaoDeConteudoResponse(
        @Schema(description = "Identificador do registro", example = "0d7c5a39-2f48-4b16-8e93-a1b6d4c2f758")
        UUID id,

        @Schema(description = "Conta NATY que fez a alteracao", example = "b81d4f27-6c39-4e0a-95a2-3d7f1c8e5a64")
        UUID usuarioId,

        @Schema(description = "Tipo do item alterado", example = "TRILHA")
        EntidadeDeConteudo entidade,

        @Schema(
                description = "Identificador do item alterado. Continua valido mesmo depois da remocao do item",
                example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
        UUID entidadeId,

        @Schema(
                description =
                        "Acao registrada. Reordenar modulos registra REORDENACAO na trilha e reordenar atividades registra no modulo",
                example = "CRIACAO")
        AcaoDeConteudo acao,

        @Schema(description = "Instante da alteracao", example = "2026-11-11T14:32:10.123-03:00")
        OffsetDateTime ocorridaEm) {}
