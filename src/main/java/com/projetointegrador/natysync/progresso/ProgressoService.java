package com.projetointegrador.natysync.progresso;

import com.projetointegrador.natysync.progresso.dto.TrilhaProgressoResponse;
import com.projetointegrador.natysync.progresso.dto.VideoAssistidoResponse;
import com.projetointegrador.natysync.shared.exception.AtividadeBloqueadaException;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.AtividadeRepository;
import com.projetointegrador.natysync.trilha.Trilha;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProgressoService {

    private final TrilhaRepository trilhaRepository;
    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProgressoAtividadeRepository progressoAtividadeRepository;
    private final TrilhaProgressoMontador montador;

    public ProgressoService(
            TrilhaRepository trilhaRepository,
            AtividadeRepository atividadeRepository,
            UsuarioRepository usuarioRepository,
            ProgressoAtividadeRepository progressoAtividadeRepository,
            TrilhaProgressoMontador montador) {
        this.trilhaRepository = trilhaRepository;
        this.atividadeRepository = atividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.progressoAtividadeRepository = progressoAtividadeRepository;
        this.montador = montador;
    }

    public TrilhaProgressoResponse buscarTrilhaComProgresso(UUID trilhaId, IntegranteDaRequisicao integrante) {
        Trilha trilha = trilhaRepository
                .findById(trilhaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trilha nao encontrada: " + trilhaId));
        Map<UUID, ProgressoAtividade> progressos = progressosDoIntegrante(integrante);
        return montador.montar(trilha, montador.sequenciar(trilha, progressos), progressos);
    }

    @Transactional
    public VideoAssistidoResponse registrarVideoAssistido(UUID atividadeId, IntegranteDaRequisicao integrante) {
        Atividade atividade = atividadeRepository
                .findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade nao encontrada: " + atividadeId));
        Trilha trilha = atividade.getModulo().getTrilha();
        Map<UUID, ProgressoAtividade> progressos = progressosDoIntegrante(integrante);
        SequenciaDaTrilha sequencia = montador.sequenciar(trilha, progressos);

        if (sequencia.estadoDe(atividadeId) == EstadoAtividade.BLOQUEADO) {
            throw new AtividadeBloqueadaException("Atividade ainda nao liberada para o integrante: " + atividadeId);
        }

        ProgressoAtividade progresso = progressos.get(atividadeId);
        if (progresso == null || progresso.getConcluidoEm() == null) {
            progresso = concluir(atividade, integrante, progresso);
            progressos.put(atividadeId, progresso);
            sequencia = montador.sequenciar(trilha, progressos);
        }

        return new VideoAssistidoResponse(
                atividadeId,
                sequencia.estadoDe(atividadeId),
                progresso.getVideoAssistidoEm(),
                progresso.getConcluidoEm(),
                sequencia.proximaAtividadeId());
    }

    private ProgressoAtividade concluir(
            Atividade atividade, IntegranteDaRequisicao integrante, ProgressoAtividade existente) {
        ProgressoAtividade progresso = existente;
        if (progresso == null) {
            progresso = new ProgressoAtividade();
            progresso.setUsuario(usuarioRepository.getReferenceById(integrante.usuarioId()));
            progresso.setAtividade(atividade);
        }
        OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
        if (progresso.getVideoAssistidoEm() == null) {
            progresso.setVideoAssistidoEm(agora);
        }
        progresso.setConcluidoEm(agora);
        return progressoAtividadeRepository.save(progresso);
    }

    private Map<UUID, ProgressoAtividade> progressosDoIntegrante(IntegranteDaRequisicao integrante) {
        return progressoAtividadeRepository.findByUsuarioId(integrante.usuarioId()).stream()
                .collect(Collectors.toMap(
                        progresso -> progresso.getAtividade().getId(),
                        Function.identity(),
                        (primeiro, segundo) -> primeiro,
                        HashMap::new));
    }
}
