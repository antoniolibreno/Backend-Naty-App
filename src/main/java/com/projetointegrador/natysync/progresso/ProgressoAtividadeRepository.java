package com.projetointegrador.natysync.progresso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProgressoAtividadeRepository extends JpaRepository<ProgressoAtividade, UUID> {

    List<ProgressoAtividade> findByUsuarioId(UUID usuarioId);

    Optional<ProgressoAtividade> findByUsuarioIdAndAtividadeId(UUID usuarioId, UUID atividadeId);

    @Query("select count(p) from ProgressoAtividade p where p.usuario.id = :usuarioId and p.concluidoEm is not null")
    long contarAtividadesConcluidas(@Param("usuarioId") UUID usuarioId);

    @Query("""
            select count(m) from Modulo m
            where exists (select a from Atividade a where a.modulo = m)
              and not exists (
                select a from Atividade a
                where a.modulo = m
                  and not exists (
                    select p from ProgressoAtividade p
                    where p.atividade = a and p.usuario.id = :usuarioId and p.concluidoEm is not null))
            """)
    long contarModulosConcluidos(@Param("usuarioId") UUID usuarioId);

    @Query("""
            select count(t) from Trilha t
            where exists (select a from Atividade a where a.modulo.trilha = t)
              and not exists (
                select a from Atividade a
                where a.modulo.trilha = t
                  and not exists (
                    select p from ProgressoAtividade p
                    where p.atividade = a and p.usuario.id = :usuarioId and p.concluidoEm is not null))
            """)
    long contarTrilhasConcluidas(@Param("usuarioId") UUID usuarioId);
}
