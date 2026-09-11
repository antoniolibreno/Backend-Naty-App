package com.projetointegrador.natysync.progresso.dto;

import java.util.List;
import java.util.UUID;

public record TrilhaProgressoResponse(
        UUID id,
        String titulo,
        String descricao,
        Integer ordem,
        int totalAtividades,
        int atividadesConcluidas,
        int percentualConcluido,
        UUID proximaAtividadeId,
        List<ModuloProgressoResponse> modulos) {}
