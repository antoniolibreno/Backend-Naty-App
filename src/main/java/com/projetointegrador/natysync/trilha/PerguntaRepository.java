package com.projetointegrador.natysync.trilha;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerguntaRepository extends JpaRepository<Pergunta, UUID> {
    boolean existsByQuizIdAndOrdem(UUID quizId, Integer ordem);

    boolean existsByQuizIdAndOrdemAndIdNot(UUID quizId, Integer ordem, UUID id);
}
