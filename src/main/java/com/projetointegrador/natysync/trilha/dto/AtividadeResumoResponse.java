package com.projetointegrador.natysync.trilha.dto;

import java.util.UUID;

public record AtividadeResumoResponse(
        UUID id,
        String titulo,
        String descricao,
        Integer ordem,
        String imagemUrl,
        Integer duracaoSegundos,
        Integer xp) {}
