package com.projetointegrador.natysync.gamificacao;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SequenciaIntegranteRepository extends JpaRepository<SequenciaIntegrante, UUID> {}
