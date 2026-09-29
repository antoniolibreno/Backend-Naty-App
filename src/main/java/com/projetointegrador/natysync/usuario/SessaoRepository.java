package com.projetointegrador.natysync.usuario;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {

    @Query("select s from Sessao s join fetch s.usuario u join fetch u.empresa where s.tokenHash = :tokenHash")
    Optional<Sessao> buscarComIntegrantePorTokenHash(@Param("tokenHash") String tokenHash);

    List<Sessao> findByUsuarioId(UUID usuarioId);

    @Modifying
    @Query("update Sessao s set s.revogadoEm = :momento where s.usuario.id = :usuarioId and s.revogadoEm is null")
    int revogarTodasDoIntegrante(@Param("usuarioId") UUID usuarioId, @Param("momento") OffsetDateTime momento);

    @Modifying
    @Query("update Sessao s set s.revogadoEm = :momento where s.revogadoEm is null"
            + " and s.usuario.id in (select u.id from Usuario u where u.empresa.id = :empresaId)")
    int revogarTodasDaEmpresa(@Param("empresaId") UUID empresaId, @Param("momento") OffsetDateTime momento);
}
