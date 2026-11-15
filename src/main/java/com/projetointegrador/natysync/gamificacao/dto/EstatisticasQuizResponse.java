package com.projetointegrador.natysync.gamificacao.dto;

public record EstatisticasQuizResponse(
        long tentativas, long aprovados, Integer melhorNota, double mediaNotas, double percentualAcerto) {}
