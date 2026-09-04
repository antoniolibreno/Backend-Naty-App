package com.projetointegrador.natysync.trilha.dto;

import java.util.List;
import java.util.UUID;

public record TrilhaDetalheResponse(
        UUID id, String titulo, String descricao, Integer ordem, List<ModuloResponse> modulos) {}
