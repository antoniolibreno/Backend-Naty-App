package com.projetointegrador.natysync.painel.dto;

import com.projetointegrador.natysync.usuario.Papel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

@Schema(description = "Novos dados cadastrais. Senha e ativação têm rota própria")
public record IntegranteAlteracaoRequest(
        @Schema(
                description = "Nome do integrante",
                example = "Ana Souza",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String nome,

        @Schema(
                description = "E-mail único no sistema, em minúsculas",
                example = "ana.souza@acme.com.br",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Email @Size(max = 255) String email,

        @Schema(
                description = "INTEGRANTE ou ADMIN. NATY é recusado",
                example = "INTEGRANTE",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Papel papel,

        @Schema(
                description = "Função no WhatsApp. Ausente mantém o atual",
                example = "user",
                allowableValues = {"admin", "supervisor", "user"})
        @Pattern(regexp = PERFIS) String perfil) {

    static final String PERFIS = "admin|supervisor|user";
    static final String PERFIL_PADRAO = "user";

    public IntegranteAlteracaoRequest {
        if (nome != null) {
            nome = nome.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
        if (perfil != null && perfil.isBlank()) {
            perfil = null;
        }
    }

    @Schema(hidden = true)
    @AssertTrue(message = "papel NATY nao e atribuido pelo painel") public boolean isPapelAtribuivel() {
        return papel != Papel.NATY;
    }
}
