package com.projetointegrador.natysync.gamificacao.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConquistaResponse(UUID id, String codigo, String titulo, String descricao, OffsetDateTime concedidaEm) {}
