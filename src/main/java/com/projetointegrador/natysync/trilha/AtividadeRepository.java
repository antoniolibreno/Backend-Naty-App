package com.projetointegrador.natysync.trilha;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AtividadeRepository extends JpaRepository<Atividade, UUID> {
    List<Atividade> findByModuloIdOrderByOrdemAsc(UUID moduloId);

    boolean existsByModuloIdAndOrdem(UUID moduloId, Integer ordem);

    boolean existsByModuloIdAndOrdemAndIdNot(UUID moduloId, Integer ordem, UUID id);
}
