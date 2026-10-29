package com.projetointegrador.natysync.gamificacao;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PontoConcedidoRepository extends JpaRepository<PontoConcedido, UUID> {

    @Modifying
    @Query(
            value = "insert into ponto_concedido (usuario_id, atividade_id, xp, concedido_em)"
                    + " values (:usuarioId, :atividadeId, :xp, :concedidoEm)"
                    + " on conflict (usuario_id, atividade_id) do nothing",
            nativeQuery = true)
    int conceder(
            @Param("usuarioId") UUID usuarioId,
            @Param("atividadeId") UUID atividadeId,
            @Param("xp") int xp,
            @Param("concedidoEm") OffsetDateTime concedidoEm);

    @Query("select coalesce(sum(p.xp), 0) from PontoConcedido p where p.usuarioId = :usuarioId")
    long somarDoIntegrante(@Param("usuarioId") UUID usuarioId);
}
