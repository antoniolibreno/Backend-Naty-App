package com.projetointegrador.natysync.progresso.dto;

import java.util.UUID;

public record ProximaAtividadeResponse(UUID atividadeId, UUID trilhaId, String titulo) {}
