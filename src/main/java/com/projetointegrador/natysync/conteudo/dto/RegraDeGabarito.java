package com.projetointegrador.natysync.conteudo.dto;

import java.util.List;

final class RegraDeGabarito {

    private RegraDeGabarito() {}

    static boolean exatamenteUmaCorreta(List<AlternativaRequest> alternativas) {
        return alternativas != null
                && alternativas.stream()
                                .filter(alternativa ->
                                        alternativa != null && Boolean.TRUE.equals(alternativa.correta()))
                                .count()
                        == 1;
    }
}
