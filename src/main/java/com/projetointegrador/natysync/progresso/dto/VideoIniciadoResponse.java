package com.projetointegrador.natysync.progresso.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VideoIniciadoResponse(UUID atividadeId, OffsetDateTime videoIniciadoEm, Integer duracaoSegundos) {}
