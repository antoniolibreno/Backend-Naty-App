package com.projetointegrador.natysync.gamificacao;

import java.util.function.ToLongFunction;

public enum CriterioConquista {
    ATIVIDADES_CONCLUIDAS(IndicadoresDoIntegrante::atividadesConcluidas),
    MAIOR_NOTA_EM_QUIZ(IndicadoresDoIntegrante::maiorNota),
    MODULOS_CONCLUIDOS(IndicadoresDoIntegrante::modulosConcluidos),
    TRILHAS_CONCLUIDAS(IndicadoresDoIntegrante::trilhasConcluidas),
    MAIOR_SEQUENCIA(IndicadoresDoIntegrante::maiorSequencia),
    PONTOS(IndicadoresDoIntegrante::pontos);

    private final ToLongFunction<IndicadoresDoIntegrante> leitor;

    CriterioConquista(ToLongFunction<IndicadoresDoIntegrante> leitor) {
        this.leitor = leitor;
    }

    public long indicador(IndicadoresDoIntegrante indicadores) {
        return leitor.applyAsLong(indicadores);
    }
}
