package com.projetointegrador.natysync.progresso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressoAtividadeRepository extends JpaRepository<ProgressoAtividade, UUID> {

    List<ProgressoAtividade> findByUsuarioId(UUID usuarioId);

    Optional<ProgressoAtividade> findByUsuarioIdAndAtividadeId(UUID usuarioId, UUID atividadeId);
}
