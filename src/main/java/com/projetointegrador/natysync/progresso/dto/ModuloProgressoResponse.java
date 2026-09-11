package com.projetointegrador.natysync.progresso.dto;

import java.util.List;
import java.util.UUID;

public record ModuloProgressoResponse(
        UUID id, String titulo, String descricao, Integer ordem, List<AtividadeProgressoResponse> atividades) {}
