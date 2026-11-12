package com.projetointegrador.natysync.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Sessao emitida: identidade do integrante e token opaco para o cabecalho Authorization")
public record SessaoResponse(
        @Schema(
                description = "Identificador do integrante autenticado",
                example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
        UUID usuarioId,

        @Schema(
                description = "Identificador da empresa do integrante",
                example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
        UUID empresaId,

        @Schema(description = "Nome do integrante", example = "Ana Souza")
        String nome,

        @Schema(description = "E-mail do integrante", example = "ana.souza@acme.com.br")
        String email,

        @Schema(
                description = "Token opaco da sessao. Aparece em claro so nesta resposta e viaja nas demais"
                        + " chamadas em Authorization: Bearer",
                example = "q7Xk2mN9pLw4sT8vB1cZ6dF3hJ5rY0uAeG2iO7nMxKc")
        String token,

        @Schema(description = "Instante em que o token expira", example = "2026-11-11T22:32:10.123-03:00")
        OffsetDateTime expiraEm) {

    @Override
    public String toString() {
        return "SessaoResponse[usuarioId=" + usuarioId + ", empresaId=" + empresaId + ", nome=" + nome + ", email="
                + email + ", token=***, expiraEm=" + expiraEm + "]";
    }
}
