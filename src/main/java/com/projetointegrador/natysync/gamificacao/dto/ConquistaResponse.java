package com.projetointegrador.natysync.gamificacao.dto;

import java.time.OffsetDateTime;

public record ConquistaResponse(String codigo, String nome, String descricao, OffsetDateTime obtidaEm) {}
