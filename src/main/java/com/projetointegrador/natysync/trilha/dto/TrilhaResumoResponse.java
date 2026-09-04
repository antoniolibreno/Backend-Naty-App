package com.projetointegrador.natysync.trilha.dto;

import java.util.UUID;

public record TrilhaResumoResponse(UUID id, String titulo, String descricao, Integer ordem) {}
