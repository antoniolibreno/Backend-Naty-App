package com.projetointegrador.natysync.trilha;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PerguntaRepository extends JpaRepository<Pergunta, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pergunta p where p.id = :id")
    Optional<Pergunta> travarPorId(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Alternativa a join a.pergunta p where a.id = :alternativaId")
    Optional<Pergunta> travarDaAlternativa(@Param("alternativaId") UUID alternativaId);

    @Query("select coalesce(max(p.ordem), 0) from Pergunta p where p.quiz.id = :quizId")
    int maiorOrdemDoQuiz(@Param("quizId") UUID quizId);

    @Query("select count(p) from Pergunta p where p.quiz.id = :quizId")
    long contarDoQuiz(@Param("quizId") UUID quizId);

    @Query(value = "select exists (select 1 from resposta_tentativa r where r.pergunta_id = :id)", nativeQuery = true)
    boolean temResposta(@Param("id") UUID id);
}
