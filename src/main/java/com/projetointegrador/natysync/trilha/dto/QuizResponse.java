package com.projetointegrador.natysync.trilha.dto;

import java.util.List;
import java.util.UUID;

public record QuizResponse(UUID id, UUID atividadeId, Integer notaMinima, List<PerguntaResponse> perguntas) {}
