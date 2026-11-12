package com.projetointegrador.natysync.conteudo.dto;

import java.util.UUID;

public record TrilhaAdminResponse(UUID id, String titulo, String descricao, Integer ordem, boolean ativa) {}
