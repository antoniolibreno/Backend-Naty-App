package com.projetointegrador.natysync.progresso.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TentativaQuizResponse(
        UUID tentativaId,
        UUID atividadeId,
        int nota,
        boolean aprovado,
        OffsetDateTime criadaEm,
        List<PerguntaErrada> perguntasErradas,
        OffsetDateTime concluidoEm,
        UUID proximaAtividadeId) {

    public record PerguntaErrada(UUID perguntaId) {}
}
