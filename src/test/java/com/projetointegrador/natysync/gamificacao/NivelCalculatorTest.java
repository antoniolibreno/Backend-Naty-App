package com.projetointegrador.natysync.gamificacao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NivelCalculatorTest {

    @Test
    void calculaNivelProgressivo() {
        assertThat(NivelCalculator.calcular(0).nivel()).isEqualTo(1);
        assertThat(NivelCalculator.calcular(99).nivel()).isEqualTo(1);
        assertThat(NivelCalculator.calcular(100).nivel()).isEqualTo(2);
        assertThat(NivelCalculator.calcular(399).nivel()).isEqualTo(2);
        assertThat(NivelCalculator.calcular(400).nivel()).isEqualTo(3);
    }

    @Test
    void calculaProgressoDoNivelAtual() {
        NivelCalculator.Resumo resumo = NivelCalculator.calcular(250);

        assertThat(resumo.nivel()).isEqualTo(2);
        assertThat(resumo.xpNoNivel()).isEqualTo(150);
        assertThat(resumo.xpParaProximoNivel()).isEqualTo(300);
        assertThat(resumo.percentual()).isEqualTo(50);
    }
}
