package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.progresso.ProgressoAtividadeRepository;
import com.projetointegrador.natysync.progresso.TentativaQuizRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AvaliadorDeConquistas {

    private final ConquistaRepository conquistaRepository;
    private final ConquistaObtidaRepository conquistaObtidaRepository;
    private final PontoConcedidoRepository pontoConcedidoRepository;
    private final DiaDeEstudoRepository diaDeEstudoRepository;
    private final ProgressoAtividadeRepository progressoAtividadeRepository;
    private final TentativaQuizRepository tentativaQuizRepository;

    public AvaliadorDeConquistas(
            ConquistaRepository conquistaRepository,
            ConquistaObtidaRepository conquistaObtidaRepository,
            PontoConcedidoRepository pontoConcedidoRepository,
            DiaDeEstudoRepository diaDeEstudoRepository,
            ProgressoAtividadeRepository progressoAtividadeRepository,
            TentativaQuizRepository tentativaQuizRepository) {
        this.conquistaRepository = conquistaRepository;
        this.conquistaObtidaRepository = conquistaObtidaRepository;
        this.pontoConcedidoRepository = pontoConcedidoRepository;
        this.diaDeEstudoRepository = diaDeEstudoRepository;
        this.progressoAtividadeRepository = progressoAtividadeRepository;
        this.tentativaQuizRepository = tentativaQuizRepository;
    }

    public void avaliar(UUID usuarioId, LocalDate diaCorrente, OffsetDateTime obtidaEm) {
        Set<UUID> obtidas = conquistaObtidaRepository.findByUsuarioId(usuarioId).stream()
                .map(ConquistaObtida::getConquistaId)
                .collect(Collectors.toSet());
        List<Conquista> pendentes = conquistaRepository.findAllByOrderByOrdemAsc().stream()
                .filter(conquista -> !obtidas.contains(conquista.getId()))
                .toList();
        if (pendentes.isEmpty()) {
            return;
        }
        IndicadoresDoIntegrante indicadores = medir(usuarioId, diaCorrente);
        for (Conquista conquista : pendentes) {
            if (conquista.atingidaPor(indicadores)) {
                conquistaObtidaRepository.registrar(usuarioId, conquista.getId(), obtidaEm);
            }
        }
    }

    private IndicadoresDoIntegrante medir(UUID usuarioId, LocalDate diaCorrente) {
        SequenciaDeDias sequencia =
                SequenciaDeDias.calcular(diaDeEstudoRepository.diasDoIntegrante(usuarioId), diaCorrente);
        return new IndicadoresDoIntegrante(
                progressoAtividadeRepository.contarAtividadesConcluidas(usuarioId),
                tentativaQuizRepository.maiorNotaDoIntegrante(usuarioId),
                progressoAtividadeRepository.contarModulosConcluidos(usuarioId),
                progressoAtividadeRepository.contarTrilhasConcluidas(usuarioId),
                sequencia.maior(),
                pontoConcedidoRepository.somarDoIntegrante(usuarioId));
    }
}
