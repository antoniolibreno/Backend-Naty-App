package com.projetointegrador.natysync.usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {

    @Query("select s from Sessao s join fetch s.usuario u join fetch u.empresa where s.tokenHash = :tokenHash")
    Optional<Sessao> buscarComIntegrantePorTokenHash(@Param("tokenHash") String tokenHash);

    List<Sessao> findByUsuarioId(UUID usuarioId);
}
