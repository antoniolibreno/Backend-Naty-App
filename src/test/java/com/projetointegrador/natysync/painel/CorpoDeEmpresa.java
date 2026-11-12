package com.projetointegrador.natysync.painel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

final class CorpoDeEmpresa {

    private static final String FUSO_PADRAO = "America/Sao_Paulo";
    private static final AtomicLong SEQUENCIA = new AtomicLong(System.nanoTime() % 100_000_000_000L);

    private CorpoDeEmpresa() {}

    static Map<String, Object> de(String nome) {
        return de(nome, true, FUSO_PADRAO);
    }

    static Map<String, Object> de(String nome, boolean ativa) {
        return de(nome, ativa, FUSO_PADRAO);
    }

    static Map<String, Object> de(String nome, boolean ativa, String fuso) {
        return Map.of(
                "nome", nome,
                "ativa", ativa,
                "fusoHorario", fuso,
                "cnpj", cnpjNovo(),
                "razaoSocial", nome + " Ltda");
    }

    static Map<String, Object> comCnpj(String nome, String cnpj) {
        Map<String, Object> corpo = new HashMap<>(de(nome));
        corpo.put("cnpj", cnpj);
        return corpo;
    }

    static Map<String, Object> comRazaoSocial(String nome, String razaoSocial) {
        Map<String, Object> corpo = new HashMap<>(de(nome));
        corpo.put("razaoSocial", razaoSocial);
        return corpo;
    }

    static Map<String, Object> sem(String nome, String campo) {
        Map<String, Object> corpo = new HashMap<>(de(nome));
        corpo.remove(campo);
        return corpo;
    }

    static String comMascara(String cnpj) {
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8) + "/"
                + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
    }

    static String cnpjNovo() {
        String base = String.format("%012d", SEQUENCIA.incrementAndGet());
        for (int digitos = 0; digitos < 100; digitos++) {
            String candidato = base + String.format("%02d", digitos);
            if (Cnpj.valido(candidato)) {
                return candidato;
            }
        }
        throw new IllegalStateException("Nenhum CNPJ valido para a base " + base);
    }
}
