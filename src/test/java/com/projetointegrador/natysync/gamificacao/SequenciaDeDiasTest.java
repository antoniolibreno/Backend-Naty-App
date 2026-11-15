package com.projetointegrador.natysync.gamificacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class SequenciaDeDiasTest {

    private static final LocalDate DIA_CORRENTE = LocalDate.of(2026, 10, 28);

    @Test
    void semEstudoTudoZero() {
        assertThat(SequenciaDeDias.calcular(List.of(), DIA_CORRENTE)).isEqualTo(new SequenciaDeDias(0, 0, false));
    }

    @Test
    void estudoNoDiaCorrenteENosDoisAnteriores() {
        var sequencia = SequenciaDeDias.calcular(diasAntes(0, 1, 2), DIA_CORRENTE);

        assertThat(sequencia).isEqualTo(new SequenciaDeDias(3, 3, true));
    }

    @Test
    void sequenciaVivaSemEstudoNoDiaCorrente() {
        var sequencia = SequenciaDeDias.calcular(diasAntes(1, 2), DIA_CORRENTE);

        assertThat(sequencia).isEqualTo(new SequenciaDeDias(2, 2, false));
    }

    @Test
    void sequenciaQuebradaQuandoOUltimoEstudoEhAnteriorAoDiaAnterior() {
        var sequencia = SequenciaDeDias.calcular(diasAntes(2, 3, 4), DIA_CORRENTE);

        assertThat(sequencia).isEqualTo(new SequenciaDeDias(0, 3, false));
    }

    @Test
    void lacunaNoMeioReiniciaASequenciaAtual() {
        var sequencia = SequenciaDeDias.calcular(diasAntes(0, 2, 3), DIA_CORRENTE);

        assertThat(sequencia).isEqualTo(new SequenciaDeDias(1, 2, true));
    }

    @Test
    void maiorSequenciaAntigaNaoDiminui() {
        var sequencia = SequenciaDeDias.calcular(diasAntes(0, 1, 10, 11, 12, 13, 14), DIA_CORRENTE);

        assertThat(sequencia.atual()).isEqualTo(2);
        assertThat(sequencia.maior()).isEqualTo(5);
    }

    @Test
    void ordemDaListaNaoImporta() {
        var sequencia = SequenciaDeDias.calcular(diasAntes(2, 0, 1), DIA_CORRENTE);

        assertThat(sequencia).isEqualTo(new SequenciaDeDias(3, 3, true));
    }

    private List<LocalDate> diasAntes(int... deslocamentos) {
        return java.util.Arrays.stream(deslocamentos)
                .mapToObj(DIA_CORRENTE::minusDays)
                .toList();
    }
}
