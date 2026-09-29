package com.projetointegrador.natysync.painel.dto;

import java.util.Locale;

public record IntegranteFiltro(String busca, Boolean ativo) {

    public IntegranteFiltro {
        busca = busca == null || busca.isBlank() ? null : busca.trim().toLowerCase(Locale.ROOT);
    }
}
