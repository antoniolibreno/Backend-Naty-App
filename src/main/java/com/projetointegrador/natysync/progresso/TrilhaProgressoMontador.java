package com.projetointegrador.natysync.progresso;

import com.projetointegrador.natysync.progresso.dto.AtividadeProgressoResponse;
import com.projetointegrador.natysync.progresso.dto.ModuloProgressoResponse;
import com.projetointegrador.natysync.progresso.dto.TrilhaProgressoResponse;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.Modulo;
import com.projetointegrador.natysync.trilha.Trilha;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TrilhaProgressoMontador {

    private static final Comparator<Modulo> POR_ORDEM_DO_MODULO = Comparator.comparing(Modulo::getOrdem);
    private static final Comparator<Atividade> POR_ORDEM_DA_ATIVIDADE = Comparator.comparing(Atividade::getOrdem);

    public SequenciaDaTrilha sequenciar(Trilha trilha, Map<UUID, ProgressoAtividade> progressoPorAtividade) {
        Map<UUID, EstadoAtividade> estados = new LinkedHashMap<>();
        UUID proximaAtividadeId = null;
        int concluidas = 0;

        for (Atividade atividade : atividadesEmOrdem(trilha)) {
            boolean concluida = estaConcluida(progressoPorAtividade.get(atividade.getId()));
            if (concluida) {
                estados.put(atividade.getId(), EstadoAtividade.CONCLUIDO);
                concluidas++;
            } else if (proximaAtividadeId == null) {
                estados.put(atividade.getId(), EstadoAtividade.DISPONIVEL);
                proximaAtividadeId = atividade.getId();
            } else {
                estados.put(atividade.getId(), EstadoAtividade.BLOQUEADO);
            }
        }

        return new SequenciaDaTrilha(estados, proximaAtividadeId, estados.size(), concluidas);
    }

    public TrilhaProgressoResponse montar(
            Trilha trilha, SequenciaDaTrilha sequencia, Map<UUID, ProgressoAtividade> progressoPorAtividade) {
        List<ModuloProgressoResponse> modulos = trilha.getModulos().stream()
                .sorted(POR_ORDEM_DO_MODULO)
                .map(modulo -> montarModulo(modulo, sequencia, progressoPorAtividade))
                .toList();

        return new TrilhaProgressoResponse(
                trilha.getId(),
                trilha.getTitulo(),
                trilha.getDescricao(),
                trilha.getOrdem(),
                sequencia.totalAtividades(),
                sequencia.atividadesConcluidas(),
                sequencia.percentualConcluido(),
                sequencia.proximaAtividadeId(),
                modulos);
    }

    private List<Atividade> atividadesEmOrdem(Trilha trilha) {
        return trilha.getModulos().stream()
                .sorted(POR_ORDEM_DO_MODULO)
                .flatMap(modulo -> modulo.getAtividades().stream().sorted(POR_ORDEM_DA_ATIVIDADE))
                .toList();
    }

    private ModuloProgressoResponse montarModulo(
            Modulo modulo, SequenciaDaTrilha sequencia, Map<UUID, ProgressoAtividade> progressoPorAtividade) {
        List<AtividadeProgressoResponse> atividades = modulo.getAtividades().stream()
                .sorted(POR_ORDEM_DA_ATIVIDADE)
                .map(atividade -> montarAtividade(atividade, sequencia, progressoPorAtividade.get(atividade.getId())))
                .toList();
        return new ModuloProgressoResponse(
                modulo.getId(), modulo.getTitulo(), modulo.getDescricao(), modulo.getOrdem(), atividades);
    }

    private AtividadeProgressoResponse montarAtividade(
            Atividade atividade, SequenciaDaTrilha sequencia, ProgressoAtividade progresso) {
        return new AtividadeProgressoResponse(
                atividade.getId(),
                atividade.getTitulo(),
                atividade.getDescricao(),
                atividade.getOrdem(),
                atividade.getImagemUrl(),
                atividade.getDuracaoSegundos(),
                atividade.getXp(),
                atividade.getQuiz() != null,
                sequencia.estadoDe(atividade.getId()),
                progresso == null ? null : progresso.getVideoAssistidoEm(),
                progresso == null ? null : progresso.getConcluidoEm());
    }

    private boolean estaConcluida(ProgressoAtividade progresso) {
        return progresso != null && progresso.getConcluidoEm() != null;
    }
}
