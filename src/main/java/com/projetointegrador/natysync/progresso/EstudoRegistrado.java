package com.projetointegrador.natysync.progresso;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EstudoRegistrado(
        UUID usuarioId, UUID empresaId, UUID atividadeId, OffsetDateTime ocorridoEm, boolean atividadeConcluida) {}
