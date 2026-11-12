package com.projetointegrador.natysync.painel.dto;

import com.projetointegrador.natysync.painel.Telefone;
import com.projetointegrador.natysync.usuario.Papel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;

@Schema(description = "Primeira conta ADMIN da empresa, que é o responsável pelo contrato")
public record AdministradorInicialRequest(
        @Schema(
                description = "Nome do responsável",
                example = "Carla Mendes",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String nome,

        @Schema(
                description = "E-mail de login, único no sistema",
                example = "carla.mendes@acme.com.br",
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
                description = "Telefone com DDD, aceita máscara, de 10 a 13 dígitos",
                example = "(11) 98765-4321",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String telefone) {

    public AdministradorInicialRequest {
        if (nome != null) {
            nome = nome.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
        telefone = Telefone.normalizar(telefone);
    }

    @Schema(hidden = true)
    @AssertTrue(message = "senha acima de 72 bytes") public boolean isSenhaDentroDoLimite() {
        return SenhaRequest.cabeNoBcrypt(senha);
    }

    @Schema(hidden = true)
    @AssertTrue(message = "telefone deve ter de 10 a 13 digitos") public boolean isTelefoneValido() {
        return Telefone.valido(telefone);
    }

    public IntegranteCriacaoRequest comoIntegrante() {
        return new IntegranteCriacaoRequest(nome, email, senha, Papel.ADMIN, null, telefone);
    }

    @Override
    public String toString() {
        return "AdministradorInicialRequest[nome=" + nome + ", email=" + email + ", senha=***, telefone=" + telefone
                + "]";
    }
}
