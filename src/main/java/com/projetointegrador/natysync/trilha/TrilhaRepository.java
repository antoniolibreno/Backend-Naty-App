package com.projetointegrador.natysync.trilha;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrilhaRepository extends JpaRepository<Trilha, UUID> {

    List<Trilha> findAllByAtivaTrueOrderByOrdemAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trilha t where t.id = :id")
    Optional<Trilha> travarPorId(@Param("id") UUID id);

    @Query("select coalesce(max(t.ordem), 0) from Trilha t")
    int maiorOrdem();
}
