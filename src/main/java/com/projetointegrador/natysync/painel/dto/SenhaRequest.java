package com.projetointegrador.natysync.painel.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;

public record SenhaRequest(
        @NotBlank @Size(min = SenhaRequest.TAMANHO_MINIMO) String senha) {

    public static final int TAMANHO_MINIMO = 8;
    private static final int LIMITE_DO_BCRYPT_EM_BYTES = 72;

    static boolean cabeNoBcrypt(String senha) {
        return senha == null || senha.getBytes(StandardCharsets.UTF_8).length <= LIMITE_DO_BCRYPT_EM_BYTES;
    }

    @AssertTrue(message = "senha acima de 72 bytes") public boolean isSenhaDentroDoLimite() {
        return cabeNoBcrypt(senha);
    }

    @Override
    public String toString() {
        return "SenhaRequest[senha=***]";
    }
}
