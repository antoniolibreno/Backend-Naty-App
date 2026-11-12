package com.projetointegrador.natysync.acompanhamento.dto;

import java.util.UUID;

public record FunilDeAtividadeResponse(
        UUID atividadeId, UUID trilhaId, String titulo, long chegaram, long passaram, int percentualDePassagem) {}
