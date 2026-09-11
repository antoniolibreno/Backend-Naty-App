package com.projetointegrador.natysync.progresso.dto;

import com.projetointegrador.natysync.progresso.EstadoAtividade;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AtividadeProgressoResponse(
        UUID id,
        String titulo,
        String descricao,
        Integer ordem,
        String imagemUrl,
        Integer duracaoSegundos,
        Integer xp,
        boolean possuiQuiz,
        EstadoAtividade estado,
        OffsetDateTime videoAssistidoEm,
        OffsetDateTime concluidoEm) {}
