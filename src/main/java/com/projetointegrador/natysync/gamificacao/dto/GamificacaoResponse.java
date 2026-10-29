package com.projetointegrador.natysync.gamificacao.dto;

import java.util.List;
import java.util.UUID;

public record GamificacaoResponse(
        UUID integranteId,
        String nome,
        long pontos,
        long posicaoNoRanking,
        SequenciaResponse sequencia,
        List<ConquistaResponse> conquistas) {}
