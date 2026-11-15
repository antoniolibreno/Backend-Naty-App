package com.projetointegrador.natysync.trilha;

import com.projetointegrador.natysync.shared.exception.ConflitoException;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.trilha.dto.AtividadeEscritaRequest;
import com.projetointegrador.natysync.trilha.dto.ModuloEscritaRequest;
import com.projetointegrador.natysync.trilha.dto.PerguntaAtualizacaoRequest;
import com.projetointegrador.natysync.trilha.dto.PerguntaEscritaRequest;
import com.projetointegrador.natysync.trilha.dto.QuizEscritaRequest;
import com.projetointegrador.natysync.trilha.dto.TrilhaEscritaRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdministracaoConteudoService {

    private final TrilhaRepository trilhas;
    private final ModuloRepository modulos;
    private final AtividadeRepository atividades;
    private final QuizRepository quizzes;
    private final PerguntaRepository perguntas;
    private final AlternativaRepository alternativas;
    private final JdbcTemplate jdbc;

    public AdministracaoConteudoService(
            TrilhaRepository trilhas,
            ModuloRepository modulos,
            AtividadeRepository atividades,
            QuizRepository quizzes,
            PerguntaRepository perguntas,
            AlternativaRepository alternativas,
            JdbcTemplate jdbc) {
        this.trilhas = trilhas;
        this.modulos = modulos;
        this.atividades = atividades;
        this.quizzes = quizzes;
        this.perguntas = perguntas;
        this.alternativas = alternativas;
        this.jdbc = jdbc;
    }

    public UUID criarTrilha(TrilhaEscritaRequest request, UUID autor) {
        Trilha trilha = new Trilha();
        trilha.setTitulo(request.titulo());
        trilha.setDescricao(request.descricao());
        trilha.setOrdem(request.ordem());
        trilha.setAtiva(true);
        trilha = trilhas.save(trilha);
        auditar("TRILHA", trilha.getId(), "CRIACAO", autor);
        return trilha.getId();
    }

    public void atualizarTrilha(UUID id, TrilhaEscritaRequest request, UUID autor) {
        Trilha trilha = buscar(trilhas, id, "Trilha");
        trilha.setTitulo(request.titulo());
        trilha.setDescricao(request.descricao());
        trilha.setOrdem(request.ordem());
        auditar("TRILHA", id, "ALTERACAO", autor);
    }

    public void ativarTrilha(UUID id, boolean ativa, UUID autor) {
        Trilha trilha = buscar(trilhas, id, "Trilha");
        trilha.setAtiva(ativa);
        auditar("TRILHA", id, ativa ? "ATIVACAO" : "DESATIVACAO", autor);
    }

    public void removerTrilha(UUID id, UUID autor) {
        Trilha trilha = buscar(trilhas, id, "Trilha");
        if (temHistoricoDaTrilha(id)) {
            trilha.setAtiva(false);
            auditar("TRILHA", id, "DESATIVACAO", autor);
            return;
        }
        trilhas.delete(trilha);
        auditar("TRILHA", id, "REMOCAO", autor);
    }

    public UUID criarModulo(ModuloEscritaRequest request, UUID autor) {
        if (modulos.existsByTrilhaIdAndOrdem(request.trilhaId(), request.ordem())) {
            throw new ConflitoException("ORDEM_EM_USO", "Ja existe modulo com essa ordem na trilha");
        }
        Modulo modulo = new Modulo();
        modulo.setTrilha(buscar(trilhas, request.trilhaId(), "Trilha"));
        modulo.setTitulo(request.titulo());
        modulo.setDescricao(request.descricao());
        modulo.setOrdem(request.ordem());
        modulo = modulos.save(modulo);
        auditar("MODULO", modulo.getId(), "CRIACAO", autor);
        return modulo.getId();
    }

    public void atualizarModulo(UUID id, ModuloEscritaRequest request, UUID autor) {
        Modulo modulo = buscar(modulos, id, "Modulo");
        if (modulos.existsByTrilhaIdAndOrdemAndIdNot(request.trilhaId(), request.ordem(), id)) {
            throw new ConflitoException("ORDEM_EM_USO", "Ja existe modulo com essa ordem na trilha");
        }
        modulo.setTrilha(buscar(trilhas, request.trilhaId(), "Trilha"));
        modulo.setTitulo(request.titulo());
        modulo.setDescricao(request.descricao());
        modulo.setOrdem(request.ordem());
        auditar("MODULO", id, "ALTERACAO", autor);
    }

    public void removerModulo(UUID id, UUID autor) {
        Modulo modulo = buscar(modulos, id, "Modulo");
        if (temHistoricoDasAtividadesDoModulo(id)) {
            throw new ConflitoException("CONTEUDO_COM_HISTORICO", "Modulo possui progresso ou tentativa registrada");
        }
        modulos.delete(modulo);
        auditar("MODULO", id, "REMOCAO", autor);
    }

    public UUID criarAtividade(AtividadeEscritaRequest request, UUID autor) {
        if (atividades.existsByModuloIdAndOrdem(request.moduloId(), request.ordem())) {
            throw new ConflitoException("ORDEM_EM_USO", "Ja existe atividade com essa ordem no modulo");
        }
        Atividade atividade = new Atividade();
        preencher(atividade, request);
        atividade = atividades.save(atividade);
        auditar("ATIVIDADE", atividade.getId(), "CRIACAO", autor);
        return atividade.getId();
    }

    public void atualizarAtividade(UUID id, AtividadeEscritaRequest request, UUID autor) {
        Atividade atividade = buscar(atividades, id, "Atividade");
        if (atividades.existsByModuloIdAndOrdemAndIdNot(request.moduloId(), request.ordem(), id)) {
            throw new ConflitoException("ORDEM_EM_USO", "Ja existe atividade com essa ordem no modulo");
        }
        preencher(atividade, request);
        auditar("ATIVIDADE", id, "ALTERACAO", autor);
    }

    public void removerAtividade(UUID id, UUID autor) {
        Atividade atividade = buscar(atividades, id, "Atividade");
        if (temHistoricoDaAtividade(id)) {
            throw new ConflitoException("CONTEUDO_COM_HISTORICO", "Atividade possui progresso ou tentativa registrada");
        }
        atividades.delete(atividade);
        auditar("ATIVIDADE", id, "REMOCAO", autor);
    }

    public UUID criarQuiz(QuizEscritaRequest request, UUID autor) {
        Quiz quiz = new Quiz();
        quiz.setAtividade(buscar(atividades, request.atividadeId(), "Atividade"));
        quiz.setNotaMinima(request.notaMinima());
        quiz = quizzes.save(quiz);
        auditar("QUIZ", quiz.getId(), "CRIACAO", autor);
        return quiz.getId();
    }

    public void atualizarQuiz(UUID id, QuizEscritaRequest request, UUID autor) {
        Quiz quiz = buscar(quizzes, id, "Quiz");
        quiz.setAtividade(buscar(atividades, request.atividadeId(), "Atividade"));
        quiz.setNotaMinima(request.notaMinima());
        auditar("QUIZ", id, "ALTERACAO", autor);
    }

    public void removerQuiz(UUID id, UUID autor) {
        Quiz quiz = buscar(quizzes, id, "Quiz");
        if (temHistoricoDaAtividade(quiz.getAtividade().getId())) {
            throw new ConflitoException("CONTEUDO_COM_HISTORICO", "Quiz possui tentativa registrada");
        }
        quizzes.delete(quiz);
        auditar("QUIZ", id, "REMOCAO", autor);
    }

    public UUID criarPergunta(PerguntaEscritaRequest request, UUID autor) {
        long corretas =
                request.alternativas().stream().filter(item -> item.correta()).count();
        if (corretas != 1
                || request.alternativas().stream()
                                .map(item -> item.ordem())
                                .distinct()
                                .count()
                        != request.alternativas().size()) {
            throw new ConflitoException(
                    "GABARITO_INVALIDO", "Cada pergunta deve possuir exatamente uma alternativa correta");
        }
        if (perguntas.existsByQuizIdAndOrdem(request.quizId(), request.ordem())) {
            throw new ConflitoException("ORDEM_EM_USO", "Ja existe pergunta com essa ordem no quiz");
        }
        Pergunta pergunta = new Pergunta();
        pergunta.setQuiz(buscar(quizzes, request.quizId(), "Quiz"));
        pergunta.setEnunciado(request.enunciado());
        pergunta.setOrdem(request.ordem());
        pergunta = perguntas.save(pergunta);
        auditar("PERGUNTA", pergunta.getId(), "CRIACAO", autor);
        for (var item : request.alternativas()) {
            Alternativa alternativa = new Alternativa();
            alternativa.setPergunta(pergunta);
            alternativa.setTexto(item.texto());
            alternativa.setCorreta(item.correta());
            alternativa.setOrdem(item.ordem());
            if (alternativas.existsByPerguntaIdAndOrdem(pergunta.getId(), item.ordem())) {
                throw new ConflitoException("ORDEM_EM_USO", "Ja existe alternativa com essa ordem na pergunta");
            }
            alternativa = alternativas.save(alternativa);
            auditar("ALTERNATIVA", alternativa.getId(), "CRIACAO", autor);
        }
        return pergunta.getId();
    }

    public void atualizarPergunta(UUID id, PerguntaAtualizacaoRequest request, UUID autor) {
        Pergunta pergunta = buscar(perguntas, id, "Pergunta");
        if (perguntas.existsByQuizIdAndOrdemAndIdNot(pergunta.getQuiz().getId(), request.ordem(), id)) {
            throw new ConflitoException("ORDEM_EM_USO", "Ja existe pergunta com essa ordem no quiz");
        }
        pergunta.setEnunciado(request.enunciado());
        pergunta.setOrdem(request.ordem());
        auditar("PERGUNTA", id, "ALTERACAO", autor);
    }

    public void removerPergunta(UUID id, UUID autor) {
        buscar(perguntas, id, "Pergunta");
        boolean temTentativa = jdbc.queryForObject(
                "select exists(select 1 from tentativa_quiz t join pergunta p on p.quiz_id = t.quiz_id where p.id = ?)",
                Boolean.class,
                id);
        if (temTentativa) {
            throw new ConflitoException("CONTEUDO_COM_HISTORICO", "Pergunta possui tentativa registrada");
        }
        perguntas.deleteById(id);
        auditar("PERGUNTA", id, "REMOCAO", autor);
    }

    public UUID criarAlternativa(UUID perguntaId, String texto, boolean correta, Integer ordem, UUID autor) {
        Pergunta pergunta = buscar(perguntas, perguntaId, "Pergunta");
        if (alternativas.existsByPerguntaIdAndOrdem(perguntaId, ordem)) {
            throw new ConflitoException("ORDEM_EM_USO", "Ja existe alternativa com essa ordem na pergunta");
        }
        List<Alternativa> atuais = alternativas.findByPerguntaIdOrderByOrdemAsc(perguntaId);
        long corretasExistentes = atuais.stream().filter(Alternativa::isCorreta).count();
        if (corretasExistentes != 1) {
            throw new ConflitoException(
                    "GABARITO_INVALIDO", "Cada pergunta deve possuir exatamente uma alternativa correta");
        }
        if (correta) {
            Alternativa gabaritoAnterior =
                    atuais.stream().filter(Alternativa::isCorreta).findFirst().orElseThrow();
            gabaritoAnterior.setCorreta(false);
            auditar("ALTERNATIVA", gabaritoAnterior.getId(), "ALTERACAO", autor);
        }
        Alternativa alternativa = new Alternativa();
        alternativa.setPergunta(pergunta);
        alternativa.setTexto(texto);
        alternativa.setCorreta(correta);
        alternativa.setOrdem(ordem);
        alternativa = alternativas.save(alternativa);
        auditar("ALTERNATIVA", alternativa.getId(), "CRIACAO", autor);
        return alternativa.getId();
    }

    public void atualizarAlternativa(UUID id, String texto, boolean correta, Integer ordem, UUID autor) {
        Alternativa alternativa = buscar(alternativas, id, "Alternativa");
        UUID perguntaId = alternativa.getPergunta().getId();
        if (alternativas.existsByPerguntaIdAndOrdemAndIdNot(perguntaId, ordem, id)) {
            throw new ConflitoException("ORDEM_EM_USO", "Ja existe alternativa com essa ordem na pergunta");
        }
        long corretas = alternativas.countByPerguntaIdAndCorretaTrue(perguntaId);
        boolean mesmaCorreta = alternativa.isCorreta();
        if ((correta && corretas > (mesmaCorreta ? 1 : 0)) || (!correta && corretas == 1 && mesmaCorreta)) {
            throw new ConflitoException(
                    "GABARITO_INVALIDO", "Cada pergunta deve possuir exatamente uma alternativa correta");
        }
        if (correta && corretas == 1 && !mesmaCorreta) {
            Alternativa gabaritoAnterior = alternativas.findByPerguntaIdOrderByOrdemAsc(perguntaId).stream()
                    .filter(Alternativa::isCorreta)
                    .findFirst()
                    .orElseThrow();
            gabaritoAnterior.setCorreta(false);
            auditar("ALTERNATIVA", gabaritoAnterior.getId(), "ALTERACAO", autor);
        }
        alternativa.setTexto(texto);
        alternativa.setCorreta(correta);
        alternativa.setOrdem(ordem);
        auditar("ALTERNATIVA", id, "ALTERACAO", autor);
    }

    public void removerAlternativa(UUID id, UUID autor) {
        Alternativa alternativa = buscar(alternativas, id, "Alternativa");
        if (alternativa.isCorreta()) {
            throw new ConflitoException("GABARITO_INVALIDO", "A alternativa correta nao pode ser removida");
        }
        boolean usadaEmTentativa = jdbc.queryForObject(
                "select exists(select 1 from tentativa_quiz t join quiz q on q.id = t.quiz_id join pergunta p on p.quiz_id = q.id join alternativa a on a.pergunta_id = p.id where a.id = ?)",
                Boolean.class,
                id);
        if (usadaEmTentativa) {
            throw new ConflitoException("CONTEUDO_COM_HISTORICO", "Alternativa possui tentativa registrada");
        }
        alternativas.delete(alternativa);
        auditar("ALTERNATIVA", id, "REMOCAO", autor);
    }

    public void reordenarModulos(UUID trilhaId, List<UUID> ids, UUID autor) {
        List<Modulo> existentes = modulos.findByTrilhaIdOrderByOrdemAsc(trilhaId);
        if (existentes.size() != ids.size()
                || new java.util.HashSet<>(ids).size() != ids.size()
                || !existentes.stream()
                        .map(Modulo::getId)
                        .collect(java.util.stream.Collectors.toSet())
                        .equals(new java.util.HashSet<>(ids))) {
            throw new ConflitoException("ORDEM_INVALIDA", "A lista deve conter todos os modulos da trilha uma vez");
        }
        jdbc.update("update modulo set ordem = -ordem where trilha_id = ?", trilhaId);
        for (int i = 0; i < ids.size(); i++) {
            jdbc.update("update modulo set ordem = ? where id = ?", i + 1, ids.get(i));
            auditar("MODULO", ids.get(i), "REORDENACAO", autor);
        }
    }

    public void reordenarAtividades(UUID moduloId, List<UUID> ids, UUID autor) {
        List<Atividade> existentes = atividades.findByModuloIdOrderByOrdemAsc(moduloId);
        if (existentes.size() != ids.size()
                || new java.util.HashSet<>(ids).size() != ids.size()
                || !existentes.stream()
                        .map(Atividade::getId)
                        .collect(java.util.stream.Collectors.toSet())
                        .equals(new java.util.HashSet<>(ids))) {
            throw new ConflitoException("ORDEM_INVALIDA", "A lista deve conter todas as atividades do modulo uma vez");
        }
        jdbc.update("update atividade set ordem = -ordem where modulo_id = ?", moduloId);
        for (int i = 0; i < ids.size(); i++) {
            jdbc.update("update atividade set ordem = ? where id = ?", i + 1, ids.get(i));
            auditar("ATIVIDADE", ids.get(i), "REORDENACAO", autor);
        }
    }

    private void preencher(Atividade atividade, AtividadeEscritaRequest request) {
        atividade.setModulo(buscar(modulos, request.moduloId(), "Modulo"));
        atividade.setTitulo(request.titulo());
        atividade.setDescricao(request.descricao());
        atividade.setOrdem(request.ordem());
        atividade.setImagemUrl(request.imagemUrl());
        atividade.setDuracaoSegundos(request.duracaoSegundos());
        atividade.setXp(request.xp());
        atividade.setVideoUrl(request.videoUrl());
    }

    private boolean temHistoricoDaAtividade(UUID id) {
        return jdbc.queryForObject(
                "select exists(select 1 from progresso_atividade where atividade_id = ?) or exists(select 1 from tentativa_quiz tq join quiz q on q.id = tq.quiz_id where q.atividade_id = ?)",
                Boolean.class,
                id,
                id);
    }

    private boolean temHistoricoDasAtividadesDoModulo(UUID id) {
        return jdbc.queryForObject(
                "select exists(select 1 from progresso_atividade p join atividade a on a.id = p.atividade_id where a.modulo_id = ?) or exists(select 1 from tentativa_quiz tq join quiz q on q.id = tq.quiz_id join atividade a on a.id = q.atividade_id where a.modulo_id = ?)",
                Boolean.class,
                id,
                id);
    }

    private boolean temHistoricoDaTrilha(UUID id) {
        return jdbc.queryForObject(
                "select exists(select 1 from progresso_atividade p join atividade a on a.id = p.atividade_id join modulo m on m.id = a.modulo_id where m.trilha_id = ?) or exists(select 1 from tentativa_quiz tq join quiz q on q.id = tq.quiz_id join atividade a on a.id = q.atividade_id join modulo m on m.id = a.modulo_id where m.trilha_id = ?)",
                Boolean.class,
                id,
                id);
    }

    private void auditar(String tipo, UUID recurso, String operacao, UUID autor) {
        jdbc.update(
                "insert into auditoria_conteudo (tipo_recurso, recurso_id, operacao, autor_id) values (?, ?, ?, ?)",
                tipo,
                recurso,
                operacao,
                autor);
    }

    private static <T> T buscar(
            org.springframework.data.jpa.repository.JpaRepository<T, UUID> repository, UUID id, String tipo) {
        return repository
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(tipo + " nao encontrado: " + id));
    }
}
