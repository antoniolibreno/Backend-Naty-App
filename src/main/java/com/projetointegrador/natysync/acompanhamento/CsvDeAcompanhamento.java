package com.projetointegrador.natysync.acompanhamento;

import com.projetointegrador.natysync.acompanhamento.dto.IntegranteAcompanhadoResponse;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvDeAcompanhamento {

    private static final String BOM = "﻿";
    private static final String FIM_DE_LINHA = "\r\n";
    private static final String CABECALHO = String.join(
            ",",
            "nome",
            "email",
            "papel",
            "situacao",
            "atividades_concluidas",
            "total_atividades",
            "percentual_concluido",
            "atividade_atual",
            "ultima_conclusao",
            "dias_sem_concluir");

    private CsvDeAcompanhamento() {}

    public static String gerar(List<IntegranteAcompanhadoResponse> integrantes) {
        return BOM
                + CABECALHO
                + FIM_DE_LINHA
                + integrantes.stream()
                        .map(CsvDeAcompanhamento::linha)
                        .collect(Collectors.joining(FIM_DE_LINHA, "", integrantes.isEmpty() ? "" : FIM_DE_LINHA));
    }

    private static String linha(IntegranteAcompanhadoResponse integrante) {
        return String.join(
                ",",
                texto(integrante.nome()),
                texto(integrante.email()),
                texto(integrante.papel().name()),
                texto(integrante.situacao().name()),
                String.valueOf(integrante.atividadesConcluidas()),
                String.valueOf(integrante.totalAtividades()),
                String.valueOf(integrante.percentualConcluido()),
                texto(
                        integrante.atividadeAtual() == null
                                ? null
                                : integrante.atividadeAtual().titulo()),
                texto(
                        integrante.ultimaConclusaoEm() == null
                                ? null
                                : integrante.ultimaConclusaoEm().toString()),
                String.valueOf(integrante.diasSemConcluir()));
    }

    static String texto(String valor) {
        if (valor == null) {
            return "";
        }
        String seguro = comecaComoFormula(valor) ? "'" + valor : valor;
        boolean precisaDeAspas =
                seguro.contains(",") || seguro.contains("\"") || seguro.contains("\n") || seguro.contains("\r");
        return precisaDeAspas ? "\"" + seguro.replace("\"", "\"\"") + "\"" : seguro;
    }

    private static boolean comecaComoFormula(String valor) {
        if (valor.isEmpty()) {
            return false;
        }
        char inicial = valor.charAt(0);
        return inicial == '='
                || inicial == '+'
                || inicial == '-'
                || inicial == '@'
                || inicial == '\t'
                || inicial == '\r';
    }
}
