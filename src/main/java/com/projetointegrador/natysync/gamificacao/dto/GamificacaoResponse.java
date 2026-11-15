package com.projetointegrador.natysync.gamificacao.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GamificacaoResponse(
        UUID integranteId,
        int xpTotal,
        int nivel,
        long xpNoNivel,
        long xpParaProximoNivel,
        int percentualNivel,
        int sequenciaAtual,
        int maiorSequencia,
        LocalDate ultimoDiaAtivo,
        EstatisticasQuizResponse quizzes,
        List<ConquistaResponse> conquistas) {}
