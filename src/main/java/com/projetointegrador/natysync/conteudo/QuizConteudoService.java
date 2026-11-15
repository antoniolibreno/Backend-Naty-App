package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AlternativaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.AlternativaCriacaoRequest;
import com.projetointegrador.natysync.conteudo.dto.AlternativaRequest;
import com.projetointegrador.natysync.conteudo.dto.PerguntaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.PerguntaAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.PerguntaCriacaoRequest;
import com.projetointegrador.natysync.conteudo.dto.PerguntaRequest;
import com.projetointegrador.natysync.conteudo.dto.QuizAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.QuizAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.QuizCriacaoRequest;
import com.projetointegrador.natysync.shared.exception.ConflitoException;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.trilha.Alternativa;
import com.projetointegrador.natysync.trilha.AlternativaRepository;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.AtividadeRepository;
import com.projetointegrador.natysync.trilha.Pergunta;
import com.projetointegrador.natysync.trilha.PerguntaRepository;
import com.projetointegrador.natysync.trilha.Quiz;
import com.projetointegrador.natysync.trilha.QuizRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuizConteudoService {

    private static final int MINIMO_DE_ALTERNATIVAS = 2;

    private final AtividadeRepository atividadeRepository;
    private final QuizRepository quizRepository;
    private final PerguntaRepository perguntaRepository;
    private final AlternativaRepository alternativaRepository;
    private final ConteudoMapper mapper;
    private final AuditoriaDeConteudo auditoria;
    private final ProtecaoDeHistorico protecao;

    public QuizConteudoService(
            AtividadeRepository atividadeRepository,
            QuizRepository quizRepository,
            PerguntaRepository perguntaRepository,
            AlternativaRepository alternativaRepository,
            ConteudoMapper mapper,
            AuditoriaDeConteudo auditoria,
            ProtecaoDeHistorico protecao) {
        this.atividadeRepository = atividadeRepository;
        this.quizRepository = quizRepository;
        this.perguntaRepository = perguntaRepository;
        this.alternativaRepository = alternativaRepository;
        this.mapper = mapper;
        this.auditoria = auditoria;
        this.protecao = protecao;
    }

    @Transactional(readOnly = true)
    public QuizAdminResponse buscar(UUID quizId) {
        return mapper.paraQuiz(buscarQuiz(quizId));
    }

    public QuizAdminResponse criarQuiz(QuizCriacaoRequest requisicao, UUID usuarioId) {
        Atividade atividade = atividadeRepository
                .travarPorId(requisicao.atividadeId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Atividade nao encontrada: " + requisicao.atividadeId()));
        if (quizRepository.findByAtividadeId(atividade.getId()).isPresent()) {
            throw new ConflitoException("QUIZ_JA_EXISTE", "A atividade ja tem quiz: " + atividade.getId());
        }
        Quiz quiz = new Quiz();
        quiz.setAtividade(atividade);
        quiz.setNotaMinima(requisicao.notaMinima());
        int ordem = 1;
        for (PerguntaRequest pergunta : requisicao.perguntas()) {
            quiz.getPerguntas().add(montarPergunta(quiz, pergunta.enunciado(), pergunta.alternativas(), ordem++));
        }
        quizRepository.saveAndFlush(quiz);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.QUIZ, quiz.getId(), AcaoDeConteudo.CRIACAO);
        quiz.getPerguntas().forEach(pergunta -> registrarCriacaoDaPergunta(usuarioId, pergunta));
        return mapper.paraQuiz(quiz);
    }

    public QuizAdminResponse alterarQuiz(UUID quizId, QuizAlteracaoRequest requisicao, UUID usuarioId) {
        Quiz quiz = buscarQuiz(quizId);
        quiz.setNotaMinima(requisicao.notaMinima());
        quizRepository.saveAndFlush(quiz);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.QUIZ, quizId, AcaoDeConteudo.ALTERACAO);
        return mapper.paraQuiz(quiz);
    }

    public void removerQuiz(UUID quizId, UUID usuarioId) {
        Quiz quiz = buscarQuiz(quizId);
        protecao.exigirSemHistorico(List.of(quiz.getAtividade().getId()));
        quizRepository.delete(quiz);
        quizRepository.flush();
        auditoria.registrar(usuarioId, EntidadeDeConteudo.QUIZ, quizId, AcaoDeConteudo.REMOCAO);
    }

    public PerguntaAdminResponse criarPergunta(PerguntaCriacaoRequest requisicao, UUID usuarioId) {
        Quiz quiz = quizRepository
                .travarPorId(requisicao.quizId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quiz nao encontrado: " + requisicao.quizId()));
        int ordem = perguntaRepository.maiorOrdemDoQuiz(quiz.getId()) + 1;
        Pergunta pergunta = montarPergunta(quiz, requisicao.enunciado(), requisicao.alternativas(), ordem);
        perguntaRepository.saveAndFlush(pergunta);
        registrarCriacaoDaPergunta(usuarioId, pergunta);
        return mapper.paraPergunta(pergunta);
    }

    public PerguntaAdminResponse alterarPergunta(UUID perguntaId, PerguntaAlteracaoRequest requisicao, UUID usuarioId) {
        Pergunta pergunta = buscarPergunta(perguntaId);
        pergunta.setEnunciado(requisicao.enunciado());
        perguntaRepository.saveAndFlush(pergunta);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.PERGUNTA, perguntaId, AcaoDeConteudo.ALTERACAO);
        return mapper.paraPergunta(pergunta);
    }

    public void removerPergunta(UUID perguntaId, UUID usuarioId) {
        Quiz quiz = quizRepository
                .travarDaPergunta(perguntaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pergunta nao encontrada: " + perguntaId));
        if (perguntaRepository.contarDoQuiz(quiz.getId()) <= 1) {
            throw new ConflitoException("QUIZ_SEM_PERGUNTA", "O quiz precisa de ao menos uma pergunta.");
        }
        protecao.recusarSe(perguntaRepository.temResposta(perguntaId));
        perguntaRepository.delete(buscarPergunta(perguntaId));
        perguntaRepository.flush();
        auditoria.registrar(usuarioId, EntidadeDeConteudo.PERGUNTA, perguntaId, AcaoDeConteudo.REMOCAO);
    }

    public AlternativaAdminResponse criarAlternativa(AlternativaCriacaoRequest requisicao, UUID usuarioId) {
        Pergunta pergunta = perguntaRepository
                .travarPorId(requisicao.perguntaId())
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Pergunta nao encontrada: " + requisicao.perguntaId()));
        if (requisicao.correta()) {
            desmarcarAtualCorreta(pergunta, usuarioId);
        }
        Alternativa alternativa = new Alternativa();
        alternativa.setPergunta(pergunta);
        alternativa.setTexto(requisicao.texto());
        alternativa.setCorreta(requisicao.correta());
        alternativa.setOrdem(alternativaRepository.maiorOrdemDaPergunta(pergunta.getId()) + 1);
        pergunta.getAlternativas().add(alternativa);
        alternativaRepository.saveAndFlush(alternativa);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.ALTERNATIVA, alternativa.getId(), AcaoDeConteudo.CRIACAO);
        return mapper.paraAlternativa(alternativa);
    }

    public AlternativaAdminResponse alterarAlternativa(
            UUID alternativaId, AlternativaRequest requisicao, UUID usuarioId) {
        Pergunta pergunta = travarPerguntaDaAlternativa(alternativaId);
        Alternativa alternativa = alternativaDa(pergunta, alternativaId);
        if (!requisicao.correta() && alternativa.isCorreta()) {
            throw new ConflitoException(
                    "GABARITO_INVALIDO", "Desmarcar a unica alternativa correta deixa a pergunta sem gabarito.");
        }
        if (requisicao.correta() && !alternativa.isCorreta()) {
            desmarcarAtualCorreta(pergunta, usuarioId);
        }
        alternativa.setTexto(requisicao.texto());
        alternativa.setCorreta(requisicao.correta());
        alternativaRepository.saveAndFlush(alternativa);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.ALTERNATIVA, alternativaId, AcaoDeConteudo.ALTERACAO);
        return mapper.paraAlternativa(alternativa);
    }

    public void removerAlternativa(UUID alternativaId, UUID usuarioId) {
        Pergunta pergunta = travarPerguntaDaAlternativa(alternativaId);
        Alternativa alternativa = alternativaDa(pergunta, alternativaId);
        if (alternativa.isCorreta()) {
            throw new ConflitoException("GABARITO_INVALIDO", "A alternativa correta nao e removida.");
        }
        if (pergunta.getAlternativas().size() <= MINIMO_DE_ALTERNATIVAS) {
            throw new ConflitoException(
                    "GABARITO_INVALIDO", "A pergunta precisa de ao menos " + MINIMO_DE_ALTERNATIVAS + " alternativas.");
        }
        protecao.recusarSe(alternativaRepository.temResposta(alternativaId));
        pergunta.getAlternativas().remove(alternativa);
        alternativaRepository.delete(alternativa);
        alternativaRepository.flush();
        auditoria.registrar(usuarioId, EntidadeDeConteudo.ALTERNATIVA, alternativaId, AcaoDeConteudo.REMOCAO);
    }

    private Pergunta montarPergunta(Quiz quiz, String enunciado, List<AlternativaRequest> alternativas, int ordem) {
        Pergunta pergunta = new Pergunta();
        pergunta.setQuiz(quiz);
        pergunta.setEnunciado(enunciado);
        pergunta.setOrdem(ordem);
        int posicao = 1;
        for (AlternativaRequest dados : alternativas) {
            Alternativa alternativa = new Alternativa();
            alternativa.setPergunta(pergunta);
            alternativa.setTexto(dados.texto());
            alternativa.setCorreta(dados.correta());
            alternativa.setOrdem(posicao++);
            pergunta.getAlternativas().add(alternativa);
        }
        return pergunta;
    }

    private void registrarCriacaoDaPergunta(UUID usuarioId, Pergunta pergunta) {
        auditoria.registrar(usuarioId, EntidadeDeConteudo.PERGUNTA, pergunta.getId(), AcaoDeConteudo.CRIACAO);
        pergunta.getAlternativas()
                .forEach(alternativa -> auditoria.registrar(
                        usuarioId, EntidadeDeConteudo.ALTERNATIVA, alternativa.getId(), AcaoDeConteudo.CRIACAO));
    }

    private void desmarcarAtualCorreta(Pergunta pergunta, UUID usuarioId) {
        for (Alternativa existente : pergunta.getAlternativas()) {
            if (existente.isCorreta()) {
                existente.setCorreta(false);
                alternativaRepository.saveAndFlush(existente);
                auditoria.registrar(
                        usuarioId, EntidadeDeConteudo.ALTERNATIVA, existente.getId(), AcaoDeConteudo.ALTERACAO);
            }
        }
    }

    private Pergunta travarPerguntaDaAlternativa(UUID alternativaId) {
        return perguntaRepository
                .travarDaAlternativa(alternativaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alternativa nao encontrada: " + alternativaId));
    }

    private Alternativa alternativaDa(Pergunta pergunta, UUID alternativaId) {
        return pergunta.getAlternativas().stream()
                .filter(alternativa -> alternativa.getId().equals(alternativaId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alternativa nao encontrada: " + alternativaId));
    }

    private Quiz buscarQuiz(UUID quizId) {
        return quizRepository
                .findById(quizId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quiz nao encontrado: " + quizId));
    }

    private Pergunta buscarPergunta(UUID perguntaId) {
        return perguntaRepository
                .findById(perguntaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pergunta nao encontrada: " + perguntaId));
    }
}
