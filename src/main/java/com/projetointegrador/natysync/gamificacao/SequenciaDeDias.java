package com.projetointegrador.natysync.gamificacao;

import java.time.LocalDate;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

public record SequenciaDeDias(int atual, int maior, boolean diaCorrenteEstudado) {

    public static SequenciaDeDias calcular(Collection<LocalDate> diasDeEstudo, LocalDate diaCorrente) {
        NavigableSet<LocalDate> dias = new TreeSet<>(diasDeEstudo);
        int maior = 0;
        int corrida = 0;
        LocalDate anterior = null;
        for (LocalDate dia : dias) {
            corrida = anterior != null && anterior.plusDays(1).equals(dia) ? corrida + 1 : 1;
            maior = Math.max(maior, corrida);
            anterior = dia;
        }
        boolean diaCorrenteEstudado = dias.contains(diaCorrente);
        int atual = 0;
        for (LocalDate dia = diaCorrenteEstudado ? diaCorrente : diaCorrente.minusDays(1);
                dias.contains(dia);
                dia = dia.minusDays(1)) {
            atual++;
        }
        return new SequenciaDeDias(atual, maior, diaCorrenteEstudado);
    }
}
