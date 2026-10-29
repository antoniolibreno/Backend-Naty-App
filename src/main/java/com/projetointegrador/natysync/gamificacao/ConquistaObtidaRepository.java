package com.projetointegrador.natysync.gamificacao;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConquistaObtidaRepository extends JpaRepository<ConquistaObtida, UUID> {

    @Modifying
    @Query(
            value = "insert into conquista_obtida (usuario_id, conquista_id, obtida_em)"
                    + " values (:usuarioId, :conquistaId, :obtidaEm)"
                    + " on conflict (usuario_id, conquista_id) do nothing",
            nativeQuery = true)
    int registrar(
            @Param("usuarioId") UUID usuarioId,
            @Param("conquistaId") UUID conquistaId,
            @Param("obtidaEm") OffsetDateTime obtidaEm);

    List<ConquistaObtida> findByUsuarioId(UUID usuarioId);
}
