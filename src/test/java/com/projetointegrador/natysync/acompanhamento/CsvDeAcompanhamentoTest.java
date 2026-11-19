package com.projetointegrador.natysync.acompanhamento;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CsvDeAcompanhamentoTest {

    @Test
    void textoComumFicaIntacto() {
        assertThat(CsvDeAcompanhamento.texto("Maria")).isEqualTo("Maria");
    }

    @Test
    void textoNuloViraVazio() {
        assertThat(CsvDeAcompanhamento.texto(null)).isEmpty();
    }

    @Test
    void celulaQueParecesFormulaGanhaApostrofo() {
        for (String perigoso : new String[] {"=SOMA(A1)", "+1", "-1", "@cmd", "\tx"}) {
            assertThat(CsvDeAcompanhamento.texto(perigoso)).startsWith("'").contains(perigoso.strip());
        }
    }

    @Test
    void virgulaAspasEQuebraDeLinhaVaoEntreAspas() {
        assertThat(CsvDeAcompanhamento.texto("Silva, Ana")).isEqualTo("\"Silva, Ana\"");
        assertThat(CsvDeAcompanhamento.texto("Ana \"Bia\"")).isEqualTo("\"Ana \"\"Bia\"\"\"");
        assertThat(CsvDeAcompanhamento.texto("a\nb")).isEqualTo("\"a\nb\"");
    }

    @Test
    void formulaComVirgulaRecebeApostrofoEAspas() {
        assertThat(CsvDeAcompanhamento.texto("=A1,B1")).isEqualTo("\"'=A1,B1\"");
    }

    @Test
    void semIntegrantesSoOCabecalhoComBom() {
        String csv = CsvDeAcompanhamento.gerar(java.util.List.of());

        assertThat(csv).startsWith("﻿nome,email,papel,situacao").endsWith("dias_sem_concluir\r\n");
    }
}
