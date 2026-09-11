package com.projetointegrador.natysync.progresso;

import java.util.Map;
import java.util.UUID;

public record SequenciaDaTrilha(
        Map<UUID, EstadoAtividade> estadoPorAtividade,
        UUID proximaAtividadeId,
        int totalAtividades,
        int atividadesConcluidas) {

    public EstadoAtividade estadoDe(UUID atividadeId) {
        return estadoPorAtividade.getOrDefault(atividadeId, EstadoAtividade.BLOQUEADO);
    }

    public boolean contem(UUID atividadeId) {
        return estadoPorAtividade.containsKey(atividadeId);
    }

    public int percentualConcluido() {
        if (totalAtividades == 0) {
            return 0;
        }
        return atividadesConcluidas * 100 / totalAtividades;
    }
}
