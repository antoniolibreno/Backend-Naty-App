package com.projetointegrador.natysync.trilha;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModuloRepository extends JpaRepository<Modulo, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Modulo m where m.id = :id")
    Optional<Modulo> travarPorId(@Param("id") UUID id);

    @Query("select coalesce(max(m.ordem), 0) from Modulo m where m.trilha.id = :trilhaId")
    int maiorOrdemDaTrilha(@Param("trilhaId") UUID trilhaId);

    @Query("select m.id from Modulo m where m.trilha.id = :trilhaId")
    List<UUID> idsDaTrilha(@Param("trilhaId") UUID trilhaId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Modulo m set m.ordem = m.ordem + :deslocamento where m.trilha.id = :trilhaId")
    int deslocarOrdemDaTrilha(@Param("trilhaId") UUID trilhaId, @Param("deslocamento") int deslocamento);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Modulo m set m.ordem = :ordem where m.id = :id")
    int definirOrdem(@Param("id") UUID id, @Param("ordem") int ordem);
}
