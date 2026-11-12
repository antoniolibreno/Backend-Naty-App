package com.projetointegrador.natysync.gamificacao;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaDeEstudoRepository extends JpaRepository<DiaDeEstudo, UUID> {

    @Modifying
    @Query(
            value = "insert into dia_de_estudo (usuario_id, dia) values (:usuarioId, :dia)"
                    + " on conflict (usuario_id, dia) do nothing",
            nativeQuery = true)
    int registrar(@Param("usuarioId") UUID usuarioId, @Param("dia") LocalDate dia);

    @Query("select d.dia from DiaDeEstudo d where d.usuarioId = :usuarioId order by d.dia")
    List<LocalDate> diasDoIntegrante(@Param("usuarioId") UUID usuarioId);
}
