package com.projetointegrador.natysync.usuario.dto;

import com.projetointegrador.natysync.usuario.Papel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Dados da conta autenticada")
public record MeuPerfilResponse(
        @Schema(description = "Identificador da conta", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34") UUID id,
        @Schema(description = "Nome da pessoa", example = "Ana Souza") String nome,
        @Schema(description = "E-mail da conta", example = "ana.souza@acme.com.br") String email,
        @Schema(description = "Empresa associada à conta") Empresa empresa,
        @Schema(description = "Cargo da pessoa", example = "user") String cargo,
        @Schema(description = "Papel da conta", example = "INTEGRANTE") Papel papel) {

    @Schema(description = "Empresa associada à conta")
    public record Empresa(
            @Schema(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10") UUID id,
            @Schema(description = "Nome da empresa", example = "Acme") String nome) {}
}
