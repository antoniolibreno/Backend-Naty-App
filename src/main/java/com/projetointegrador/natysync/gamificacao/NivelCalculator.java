package com.projetointegrador.natysync.gamificacao;

public final class NivelCalculator {

    private NivelCalculator() {}

    public static Resumo calcular(int xpTotal) {
        long xp = Math.max(0, xpTotal);
        int nivel = 1;
        while (limiarDoNivel(nivel + 1) <= xp) {
            nivel++;
        }
        long inicio = limiarDoNivel(nivel);
        long proximo = limiarDoNivel(nivel + 1);
        int progresso = (int) Math.min(100, ((xp - inicio) * 100) / Math.max(1, proximo - inicio));
        return new Resumo(nivel, xp - inicio, proximo - inicio, progresso);
    }

    private static long limiarDoNivel(int nivel) {
        long anterior = Math.max(0, nivel - 1L);
        return 100L * anterior * anterior;
    }

    public record Resumo(int nivel, long xpNoNivel, long xpParaProximoNivel, int percentual) {}
}
