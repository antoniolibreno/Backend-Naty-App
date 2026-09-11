package com.projetointegrador.natysync.progresso.dto;

import com.projetointegrador.natysync.progresso.EstadoAtividade;
import java.time.OffsetDateTime;
import java.util.UUID;

public record VideoAssistidoResponse(
        UUID atividadeId,
        EstadoAtividade estado,
        OffsetDateTime videoAssistidoEm,
        OffsetDateTime concluidoEm,
        UUID proximaAtividadeId) {}
