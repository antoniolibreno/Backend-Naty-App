package com.projetointegrador.natysync.painel.dto;

import com.projetointegrador.natysync.usuario.Papel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Integrante com dados de cadastro, sem senha")
public record IntegranteResponse(
        @Schema(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
        UUID id,

        @Schema(
                description = "Identificador da empresa do integrante",
                example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
        UUID empresaId,

        @Schema(description = "Nome do integrante", example = "Ana Souza")
        String nome,

        @Schema(description = "E-mail da conta", example = "ana.souza@acme.com.br")
        String email,

        @Schema(description = "Papel da conta", example = "INTEGRANTE")
        Papel papel,

        @Schema(
                description = "Função no WhatsApp, sem efeito de permissão",
                example = "user",
                allowableValues = {"admin", "supervisor", "user"})
        String perfil,

        @Schema(description = "Indica se a conta pode entrar", example = "true")
        boolean ativo,

        @Schema(description = "Instante de criação", example = "2026-11-11T14:32:10.123-03:00")
        OffsetDateTime criadoEm,

        @Schema(description = "Instante da última alteração", example = "2026-11-11T15:10:00.000-03:00")
        OffsetDateTime atualizadoEm) {}
