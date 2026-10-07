package com.projetointegrador.natysync.progresso;

import com.projetointegrador.natysync.progresso.dto.TrilhaProgressoResponse;
import com.projetointegrador.natysync.progresso.dto.VideoAssistidoResponse;
import com.projetointegrador.natysync.progresso.dto.TentativaQuizRequest;
import com.projetointegrador.natysync.progresso.dto.TentativaQuizResponse;
import com.projetointegrador.natysync.shared.exception.AtividadeBloqueadaException;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.shared.exception.TentativaQuizInvalidaException;
import com.projetointegrador.natysync.trilha.Alternativa;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.AtividadeRepository;
import com.projetointegrador.natysync.trilha.Trilha;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import com.projetointegrador.natysync.trilha.Quiz;
import com.projetointegrador.natysync.trilha.QuizRepository;
import com.projetointegrador.natysync.trilha.Pergunta;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
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
    private final QuizRepository quizRepository;
    private final EntityManager entityManager;

    public ProgressoService(
            TrilhaRepository trilhaRepository,
            AtividadeRepository atividadeRepository,
            UsuarioRepository usuarioRepository,
            ProgressoAtividadeRepository progressoAtividadeRepository,
            TrilhaProgressoMontador montador,
            QuizRepository quizRepository,
            EntityManager entityManager) {
        this.trilhaRepository = trilhaRepository;
        this.atividadeRepository = atividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.progressoAtividadeRepository = progressoAtividadeRepository;
        this.montador = montador;
        this.quizRepository = quizRepository;
        this.entityManager = entityManager;
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
            throw new AtividadeBloqueadaException("Atividade nao liberada para o integrante: " + atividadeId);
        }

        ProgressoAtividade progresso = progressos.get(atividadeId);
        if (progresso == null) {
            progresso = novoProgresso(atividade, integrante);
        }
        if (progresso.getVideoAssistidoEm() == null) {
            progresso.setVideoAssistidoEm(agora());
            progresso = progressoAtividadeRepository.save(progresso);
        }
        if (atividade.getQuiz() == null && progresso.getConcluidoEm() == null) {
            progresso.setConcluidoEm(agora());
            progresso = progressoAtividadeRepository.save(progresso);
        }
        if (progresso.getConcluidoEm() != null) {
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

    @Transactional
    public TentativaQuizResponse registrarTentativa(
            UUID atividadeId, TentativaQuizRequest requisicao, IntegranteDaRequisicao integrante) {
        Atividade atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade nao encontrada: " + atividadeId));
        Quiz quiz = quizRepository.findByAtividadeId(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quiz nao encontrado para a atividade: " + atividadeId));
        Map<UUID, ProgressoAtividade> progressos = progressosDoIntegrante(integrante);
        Trilha trilha = atividade.getModulo().getTrilha();
        SequenciaDaTrilha sequencia = montador.sequenciar(trilha, progressos);
        if (sequencia.estadoDe(atividadeId) == EstadoAtividade.BLOQUEADO) {
            throw new AtividadeBloqueadaException("Atividade nao liberada para o integrante: " + atividadeId);
        }

        Map<UUID, Pergunta> perguntas = quiz.getPerguntas().stream()
                .collect(Collectors.toMap(Pergunta::getId, Function.identity()));
        Map<UUID, TentativaQuizRequest.Resposta> respostasEnviadas = new HashMap<>();
        for (TentativaQuizRequest.Resposta resposta : requisicao.respostas()) {
            if (respostasEnviadas.putIfAbsent(resposta.perguntaId(), resposta) != null) {
                throw tentativaInvalida("pergunta", "Uma pergunta foi respondida mais de uma vez");
            }
            if (!perguntas.containsKey(resposta.perguntaId())) {
                throw tentativaInvalida("perguntaId", "A pergunta nao pertence ao quiz da atividade");
            }
        }
        if (respostasEnviadas.size() != perguntas.size() || !respostasEnviadas.keySet().containsAll(perguntas.keySet())) {
            throw tentativaInvalida("respostas", "Todas as perguntas do quiz devem ser respondidas");
        }

        TentativaQuiz tentativa = new TentativaQuiz();
        tentativa.setUsuario(usuarioRepository.getReferenceById(integrante.usuarioId()));
        tentativa.setQuiz(quiz);
        List<Pergunta> perguntasErradas = new java.util.ArrayList<>();
        int corretas = 0;
        for (Pergunta pergunta : quiz.getPerguntas()) {
            TentativaQuizRequest.Resposta enviada = respostasEnviadas.get(pergunta.getId());
            Alternativa alternativa = pergunta.getAlternativas().stream()
                    .filter(item -> item.getId().equals(enviada.alternativaId()))
                    .findFirst()
                    .orElseThrow(() -> tentativaInvalida("alternativaId", "A alternativa nao pertence a pergunta"));
            RespostaTentativa resposta = new RespostaTentativa();
            resposta.setTentativa(tentativa);
            resposta.setPergunta(pergunta);
            resposta.setAlternativa(alternativa);
            resposta.setCorreta(alternativa.isCorreta());
            tentativa.getRespostas().add(resposta);
            if (alternativa.isCorreta()) {
                corretas++;
            } else {
                perguntasErradas.add(pergunta);
            }
        }
        int nota = (int) Math.round(corretas * 100.0 / perguntas.size());
        boolean aprovado = nota >= quiz.getNotaMinima();
        tentativa.setNota(nota);
        tentativa.setAprovado(aprovado);
        tentativa = persistirTentativa(tentativa);

        ProgressoAtividade progresso = progressos.get(atividadeId);
        if (progresso == null) {
            progresso = novoProgresso(atividade, integrante);
        }
        if (progresso.getMelhorNota() == null || nota > progresso.getMelhorNota()) {
            progresso.setMelhorNota(nota);
        }
        if (aprovado && progresso.getConcluidoEm() == null) {
            progresso.setConcluidoEm(agora());
        }
        progresso = progressoAtividadeRepository.save(progresso);
        if (progresso.getConcluidoEm() != null) {
            progressos.put(atividadeId, progresso);
        }
        sequencia = montador.sequenciar(trilha, progressos);
        return new TentativaQuizResponse(
                tentativa.getId(), atividadeId, nota, aprovado, tentativa.getCriadaEm(),
                perguntasErradas.stream().map(pergunta -> new TentativaQuizResponse.PerguntaErrada(pergunta.getId())).toList(),
                progresso.getConcluidoEm(), sequencia.proximaAtividadeId());
    }

    private ProgressoAtividade novoProgresso(
            Atividade atividade, IntegranteDaRequisicao integrante, ProgressoAtividade existente) {
        ProgressoAtividade progresso = existente;
        if (progresso == null) {
            progresso = new ProgressoAtividade();
            progresso.setUsuario(usuarioRepository.getReferenceById(integrante.usuarioId()));
            progresso.setAtividade(atividade);
        }
        return progresso;
    }

    private ProgressoAtividade novoProgresso(Atividade atividade, IntegranteDaRequisicao integrante) {
        return novoProgresso(atividade, integrante, null);
    }

    private OffsetDateTime agora() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
    }

    private TentativaQuizInvalidaException tentativaInvalida(String campo, String mensagem) {
        return new TentativaQuizInvalidaException(campo, mensagem);
    }

    private TentativaQuiz persistirTentativa(TentativaQuiz tentativa) {
        return entityManager.merge(tentativa);
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
