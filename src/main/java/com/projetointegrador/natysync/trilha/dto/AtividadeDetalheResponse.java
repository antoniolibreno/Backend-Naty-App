package com.projetointegrador.natysync.trilha.dto;

import java.util.UUID;

public record AtividadeDetalheResponse(
        UUID id,
        String titulo,
        String descricao,
        Integer ordem,
        String imagemUrl,
        String videoUrl,
        Integer duracaoSegundos,
        Integer xp,
        boolean possuiQuiz) {}
