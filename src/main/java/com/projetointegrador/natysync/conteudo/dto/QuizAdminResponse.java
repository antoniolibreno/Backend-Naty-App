package com.projetointegrador.natysync.conteudo.dto;

import java.util.List;
import java.util.UUID;

public record QuizAdminResponse(UUID id, UUID atividadeId, Integer notaMinima, List<PerguntaAdminResponse> perguntas) {}
