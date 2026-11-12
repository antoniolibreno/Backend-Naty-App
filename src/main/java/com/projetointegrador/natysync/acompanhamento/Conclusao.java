package com.projetointegrador.natysync.acompanhamento;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Conclusao(UUID usuarioId, UUID atividadeId, OffsetDateTime concluidoEm) {}
