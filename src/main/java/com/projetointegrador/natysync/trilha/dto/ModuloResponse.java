package com.projetointegrador.natysync.trilha.dto;

import java.util.List;
import java.util.UUID;

public record ModuloResponse(
        UUID id, String titulo, String descricao, Integer ordem, List<AtividadeResumoResponse> atividades) {}
