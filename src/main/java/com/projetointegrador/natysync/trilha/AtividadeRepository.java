package com.projetointegrador.natysync.trilha;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AtividadeRepository extends JpaRepository<Atividade, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Atividade a where a.id = :id")
    Optional<Atividade> travarPorId(@Param("id") UUID id);

    @Query("select coalesce(max(a.ordem), 0) from Atividade a where a.modulo.id = :moduloId")
    int maiorOrdemDoModulo(@Param("moduloId") UUID moduloId);

    @Query("select a.id from Atividade a where a.modulo.id = :moduloId")
    List<UUID> idsDoModulo(@Param("moduloId") UUID moduloId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Atividade a set a.ordem = a.ordem + :deslocamento where a.modulo.id = :moduloId")
    int deslocarOrdemDoModulo(@Param("moduloId") UUID moduloId, @Param("deslocamento") int deslocamento);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Atividade a set a.ordem = :ordem where a.id = :id")
    int definirOrdem(@Param("id") UUID id, @Param("ordem") int ordem);

    @Query(value = """
            select exists (select 1 from progresso_atividade p where p.atividade_id in (:ids))
                or exists (select 1 from ponto_concedido c where c.atividade_id in (:ids))
                or exists (select 1 from tentativa_quiz t join quiz q on q.id = t.quiz_id
                           where q.atividade_id in (:ids))
            """, nativeQuery = true)
    boolean temHistoricoDeEstudo(@Param("ids") Collection<UUID> ids);
}
