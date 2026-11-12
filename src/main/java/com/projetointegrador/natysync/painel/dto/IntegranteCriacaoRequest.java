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

@Schema(description = "Dados de cadastro do integrante")
public record IntegranteCriacaoRequest(
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
                description = "De 8 caracteres a 72 bytes",
                example = "senha-exemplo-123",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 8)
        @NotBlank @Size(min = SenhaRequest.TAMANHO_MINIMO) String senha,

        @Schema(
                description = "INTEGRANTE ou ADMIN. NATY é recusado",
                example = "INTEGRANTE",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Papel papel,

        @Schema(
                description = "Função no WhatsApp. Ausente assume user",
                example = "user",
                allowableValues = {"admin", "supervisor", "user"})
        @Pattern(regexp = IntegranteAlteracaoRequest.PERFIS) String perfil) {

    public IntegranteCriacaoRequest {
        if (nome != null) {
            nome = nome.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
        if (perfil == null || perfil.isBlank()) {
            perfil = IntegranteAlteracaoRequest.PERFIL_PADRAO;
        }
    }

    @Schema(hidden = true)
    @AssertTrue(message = "papel NATY nao e atribuido pelo painel") public boolean isPapelAtribuivel() {
        return papel != Papel.NATY;
    }

    @Schema(hidden = true)
    @AssertTrue(message = "senha acima de 72 bytes") public boolean isSenhaDentroDoLimite() {
        return SenhaRequest.cabeNoBcrypt(senha);
    }

    @Override
    public String toString() {
        return "IntegranteCriacaoRequest[nome=" + nome + ", email=" + email + ", senha=***, papel=" + papel
                + ", perfil=" + perfil + "]";
    }
}
