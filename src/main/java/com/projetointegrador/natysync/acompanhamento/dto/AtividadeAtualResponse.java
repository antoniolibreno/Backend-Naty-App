package com.projetointegrador.natysync.acompanhamento.dto;

import java.util.UUID;

public record AtividadeAtualResponse(UUID atividadeId, UUID trilhaId, String titulo) {}
