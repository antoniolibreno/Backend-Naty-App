package com.projetointegrador.natysync.gamificacao.dto;

import java.util.UUID;

public record RankingItemResponse(
        UUID integranteId, String nome, int posicao, int xpTotal, int nivel, int sequenciaAtual) {}
