package com.projetointegrador.natysync.progresso;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TentativaQuizRepository extends JpaRepository<TentativaQuiz, UUID> {

    List<TentativaQuiz> findByUsuarioIdAndQuizIdOrderByCriadaEmDesc(UUID usuarioId, UUID quizId);

    @Query("select coalesce(max(t.nota), 0) from TentativaQuiz t where t.usuario.id = :usuarioId")
    int maiorNotaDoIntegrante(@Param("usuarioId") UUID usuarioId);
}
