package com.projetointegrador.natysync.painel.dto;

import com.projetointegrador.natysync.usuario.Papel;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record IntegranteAlteracaoRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull Papel papel,
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

    @AssertTrue(message = "papel NATY nao e atribuido pelo painel") public boolean isPapelAtribuivel() {
        return papel != Papel.NATY;
    }
}
