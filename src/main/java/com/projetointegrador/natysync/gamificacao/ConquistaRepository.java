package com.projetointegrador.natysync.gamificacao;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConquistaRepository extends JpaRepository<Conquista, UUID> {

    Optional<Conquista> findByCodigoAndAtivaTrue(String codigo);
}
