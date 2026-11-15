package com.projetointegrador.natysync.trilha;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuloRepository extends JpaRepository<Modulo, UUID> {
    List<Modulo> findByTrilhaIdOrderByOrdemAsc(UUID trilhaId);

    boolean existsByTrilhaIdAndOrdem(UUID trilhaId, Integer ordem);

    boolean existsByTrilhaIdAndOrdemAndIdNot(UUID trilhaId, Integer ordem, UUID id);
}
