package com.projetointegrador.natysync.gamificacao;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConquistaUsuarioRepository extends JpaRepository<ConquistaUsuario, ConquistaUsuarioId> {

    boolean existsByUsuarioIdAndConquistaId(UUID usuarioId, UUID conquistaId);

    List<ConquistaUsuario> findByUsuarioIdOrderByConcedidaEm(UUID usuarioId);
}
