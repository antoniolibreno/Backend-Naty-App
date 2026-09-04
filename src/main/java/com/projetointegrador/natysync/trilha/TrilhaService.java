package com.projetointegrador.natysync.trilha;

import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.trilha.dto.AtividadeDetalheResponse;
import com.projetointegrador.natysync.trilha.dto.QuizResponse;
import com.projetointegrador.natysync.trilha.dto.TrilhaDetalheResponse;
import com.projetointegrador.natysync.trilha.dto.TrilhaResumoResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TrilhaService {

    private final TrilhaRepository trilhaRepository;
    private final AtividadeRepository atividadeRepository;
    private final QuizRepository quizRepository;
    private final TrilhaMapper trilhaMapper;
    private final QuizMapper quizMapper;

    public TrilhaService(
            TrilhaRepository trilhaRepository,
            AtividadeRepository atividadeRepository,
            QuizRepository quizRepository,
            TrilhaMapper trilhaMapper,
            QuizMapper quizMapper) {
        this.trilhaRepository = trilhaRepository;
        this.atividadeRepository = atividadeRepository;
        this.quizRepository = quizRepository;
        this.trilhaMapper = trilhaMapper;
        this.quizMapper = quizMapper;
    }

    public List<TrilhaResumoResponse> listarAtivas() {
        return trilhaMapper.paraResumos(trilhaRepository.findAllByAtivaTrueOrderByOrdemAsc());
    }

    public TrilhaDetalheResponse buscarDetalhe(UUID trilhaId) {
        Trilha trilha = trilhaRepository
                .findById(trilhaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trilha nao encontrada: " + trilhaId));
        return trilhaMapper.paraDetalhe(trilha);
    }

    public AtividadeDetalheResponse buscarAtividade(UUID atividadeId) {
        Atividade atividade = atividadeRepository
                .findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade nao encontrada: " + atividadeId));
        return trilhaMapper.paraAtividadeDetalhe(atividade);
    }

    public QuizResponse buscarQuizDaAtividade(UUID atividadeId) {
        Quiz quiz = quizRepository
                .findByAtividadeId(atividadeId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Quiz nao encontrado para a atividade: " + atividadeId));
        return quizMapper.paraResposta(quiz);
    }
}
