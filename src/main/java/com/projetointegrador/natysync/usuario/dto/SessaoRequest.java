package com.projetointegrador.natysync.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Locale;

public record SessaoRequest(@NotBlank @Email String email) {

    public SessaoRequest {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
