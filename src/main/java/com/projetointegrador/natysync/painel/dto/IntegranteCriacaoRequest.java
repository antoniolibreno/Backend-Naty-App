package com.projetointegrador.natysync.painel.dto;

import com.projetointegrador.natysync.usuario.Papel;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record IntegranteCriacaoRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = SenhaRequest.TAMANHO_MINIMO) String senha,
        @NotNull Papel papel,
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

    @AssertTrue(message = "papel NATY nao e atribuido pelo painel") public boolean isPapelAtribuivel() {
        return papel != Papel.NATY;
    }

    @AssertTrue(message = "senha acima de 72 bytes") public boolean isSenhaDentroDoLimite() {
        return SenhaRequest.cabeNoBcrypt(senha);
    }

    @Override
    public String toString() {
        return "IntegranteCriacaoRequest[nome=" + nome + ", email=" + email + ", senha=***, papel=" + papel
                + ", perfil=" + perfil + "]";
    }
}
