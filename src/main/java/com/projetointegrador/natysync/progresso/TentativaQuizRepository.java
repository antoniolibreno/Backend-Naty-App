package com.projetointegrador.natysync.progresso;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativaQuizRepository extends JpaRepository<TentativaQuiz, UUID> {

    List<TentativaQuiz> findByUsuarioIdAndQuizIdOrderByCriadaEmDesc(UUID usuarioId, UUID quizId);
}
