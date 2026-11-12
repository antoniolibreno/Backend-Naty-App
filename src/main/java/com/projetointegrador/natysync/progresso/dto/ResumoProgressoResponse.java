package com.projetointegrador.natysync.progresso.dto;

import java.util.UUID;

public record ResumoProgressoResponse(
        UUID integranteId,
        int totalAtividades,
        int atividadesConcluidas,
        int percentualConcluido,
        ProximaAtividadeResponse proximaAtividade) {}
