package com.projetointegrador.natysync.trilha;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    Optional<Quiz> findByAtividadeId(UUID atividadeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Quiz q where q.id = :id")
    Optional<Quiz> travarPorId(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Pergunta p join p.quiz q where p.id = :perguntaId")
    Optional<Quiz> travarDaPergunta(@Param("perguntaId") UUID perguntaId);
}
