package com.projetointegrador.natysync.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Locale;

public record SessaoRequest(
        @NotBlank @Email String email, @NotBlank String senha) {

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
