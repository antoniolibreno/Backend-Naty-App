package com.projetointegrador.natysync.gamificacao.dto;

import java.util.UUID;

public record PosicaoRankingResponse(long posicao, UUID integranteId, String nome, long pontos) {}
