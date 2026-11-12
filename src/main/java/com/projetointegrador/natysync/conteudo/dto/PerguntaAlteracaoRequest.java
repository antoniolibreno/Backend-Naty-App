package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.constraints.NotBlank;

public record PerguntaAlteracaoRequest(@NotBlank String enunciado) {

    public PerguntaAlteracaoRequest {
        if (enunciado != null) {
            enunciado = enunciado.trim();
        }
    }
}
