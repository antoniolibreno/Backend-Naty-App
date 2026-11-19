package com.projetointegrador.natysync.gamificacao;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XpAtividadeRepository extends JpaRepository<XpAtividade, UUID> {

    boolean existsByUsuarioIdAndAtividadeId(UUID usuarioId, UUID atividadeId);
}
