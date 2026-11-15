package com.projetointegrador.natysync.trilha;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlternativaRepository extends JpaRepository<Alternativa, UUID> {

    @Query("select coalesce(max(a.ordem), 0) from Alternativa a where a.pergunta.id = :perguntaId")
    int maiorOrdemDaPergunta(@Param("perguntaId") UUID perguntaId);

    @Query(
            value = "select exists (select 1 from resposta_tentativa r where r.alternativa_id = :id)",
            nativeQuery = true)
    boolean temResposta(@Param("id") UUID id);
}
