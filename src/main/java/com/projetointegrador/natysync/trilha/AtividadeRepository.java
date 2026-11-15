package com.projetointegrador.natysync.trilha;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtividadeRepository extends JpaRepository<Atividade, UUID> {
    List<Atividade> findByModuloIdOrderByOrdemAsc(UUID moduloId);

    boolean existsByModuloIdAndOrdem(UUID moduloId, Integer ordem);

    boolean existsByModuloIdAndOrdemAndIdNot(UUID moduloId, Integer ordem, UUID id);
}
