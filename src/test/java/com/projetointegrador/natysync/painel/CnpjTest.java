package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CnpjTest {

    @ParameterizedTest
    @ValueSource(strings = {"11222333000181", "48055059000103", "12ABC34501DE35"})
    void aceitaCnpjComDigitosVerificadoresCorretos(String cnpj) {
        assertThat(Cnpj.valido(cnpj)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "11222333000182",
                "00000000000000",
                "AAAAAAAAAAAA00",
                "1122233300018",
                "112223330001810",
                "11222333000A81",
                "11222333000181 ",
                ""
            })
    void recusaCnpjInvalido(String cnpj) {
        assertThat(Cnpj.valido(cnpj)).isFalse();
    }

    @Test
    void recusaNulo() {
        assertThat(Cnpj.valido(null)).isFalse();
    }

    @Test
    void normalizaRemovendoSeparadoresEPassandoParaMaiusculas() {
        assertThat(Cnpj.normalizar("12.abc.345/01de-35")).isEqualTo("12ABC34501DE35");
        assertThat(Cnpj.normalizar(" 11.222.333/0001-81 ")).isEqualTo("11222333000181");
    }

    @Test
    void normalizarNuloDevolveNulo() {
        assertThat(Cnpj.normalizar(null)).isNull();
    }
}
