package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PerguntaRequest(
        @NotBlank String enunciado, @Size(min = 2) List<@Valid AlternativaRequest> alternativas) {

    public PerguntaRequest {
        if (enunciado != null) {
            enunciado = enunciado.trim();
        }
    }

    @AssertTrue(message = "a pergunta deve ter exatamente uma alternativa correta") public boolean isUmaAlternativaCorreta() {
        return RegraDeGabarito.exatamenteUmaCorreta(alternativas);
    }
}
