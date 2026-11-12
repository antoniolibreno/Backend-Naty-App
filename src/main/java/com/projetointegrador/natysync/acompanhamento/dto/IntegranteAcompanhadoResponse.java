package com.projetointegrador.natysync.acompanhamento.dto;

import com.projetointegrador.natysync.acompanhamento.SituacaoDeAcompanhamento;
import com.projetointegrador.natysync.usuario.Papel;
import java.time.OffsetDateTime;
import java.util.UUID;

public record IntegranteAcompanhadoResponse(
        UUID integranteId,
        String nome,
        String email,
        Papel papel,
        SituacaoDeAcompanhamento situacao,
        int atividadesConcluidas,
        int totalAtividades,
        int percentualConcluido,
        AtividadeAtualResponse atividadeAtual,
        OffsetDateTime ultimaConclusaoEm,
        long diasSemConcluir) {}
