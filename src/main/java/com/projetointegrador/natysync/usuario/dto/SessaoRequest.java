package com.projetointegrador.natysync.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Locale;

@Schema(description = "Credencial para abrir uma sessão")
public record SessaoRequest(
        @Schema(
                description = "E-mail da conta",
                example = "ana.souza@acme.com.br",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Email String email,

        @Schema(
                description = "Senha da conta",
                example = "senha-exemplo-123",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String senha) {

    public SessaoRequest {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public String toString() {
        return "SessaoRequest[email=" + email + ", senha=***]";
    }
}
