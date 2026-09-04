package com.projetointegrador.natysync.trilha;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    Optional<Quiz> findByAtividadeId(UUID atividadeId);
}
