package com.projetointegrador.natysync.painel;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Cnpj {

    private static final Pattern FORMATO = Pattern.compile("^[0-9A-Z]{12}[0-9]{2}$");
    private static final Pattern SEPARADORES = Pattern.compile("[./\\-\\s]");
    private static final int[] PESOS_DO_PRIMEIRO_DIGITO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_DO_SEGUNDO_DIGITO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int TAMANHO_DA_BASE = 12;

    private Cnpj() {}

    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        return SEPARADORES.matcher(texto).replaceAll("").toUpperCase(Locale.ROOT);
    }

    public static boolean valido(String cnpj) {
        if (cnpj == null
                || !FORMATO.matcher(cnpj).matches()
                || cnpj.chars().distinct().count() == 1) {
            return false;
        }
        String base = cnpj.substring(0, TAMANHO_DA_BASE);
        int primeiro = digito(base, PESOS_DO_PRIMEIRO_DIGITO);
        int segundo = digito(base + primeiro, PESOS_DO_SEGUNDO_DIGITO);
        return valor(cnpj.charAt(TAMANHO_DA_BASE)) == primeiro && valor(cnpj.charAt(TAMANHO_DA_BASE + 1)) == segundo;
    }

    private static int digito(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += valor(base.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int valor(char caractere) {
        return caractere - '0';
    }
}
