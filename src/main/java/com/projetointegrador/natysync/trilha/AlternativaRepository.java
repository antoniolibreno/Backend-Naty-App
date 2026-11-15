package com.projetointegrador.natysync.trilha;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlternativaRepository extends JpaRepository<Alternativa, UUID> {
    List<Alternativa> findByPerguntaIdOrderByOrdemAsc(UUID perguntaId);

    long countByPerguntaIdAndCorretaTrue(UUID perguntaId);

    boolean existsByPerguntaIdAndOrdem(UUID perguntaId, Integer ordem);

    boolean existsByPerguntaIdAndOrdemAndIdNot(UUID perguntaId, Integer ordem, UUID id);
}
