package com.projetointegrador.natysync.painel;

import java.util.regex.Pattern;

public final class Telefone {

    private static final Pattern NAO_DIGITO = Pattern.compile("\\D");
    private static final int MINIMO_DE_DIGITOS = 10;
    private static final int MAXIMO_DE_DIGITOS = 13;

    private Telefone() {}

    public static String normalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return NAO_DIGITO.matcher(texto).replaceAll("");
    }

    public static boolean valido(String digitos) {
        return digitos == null || (digitos.length() >= MINIMO_DE_DIGITOS && digitos.length() <= MAXIMO_DE_DIGITOS);
    }
}
