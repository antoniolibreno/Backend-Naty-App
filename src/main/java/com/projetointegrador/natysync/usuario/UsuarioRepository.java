package com.projetointegrador.natysync.usuario;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID>, JpaSpecificationExecutor<Usuario> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where lower(u.email) = :email")
    Optional<Usuario> travarPorEmailNormalizado(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id and u.empresa.id = :empresaId")
    Optional<Usuario> travarNaEmpresa(@Param("id") UUID id, @Param("empresaId") UUID empresaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.empresa.id = :empresaId")
    List<Usuario> travarTodosDaEmpresa(@Param("empresaId") UUID empresaId);

    @Query("select u from Usuario u join fetch u.empresa where u.id = :id and u.empresa.id = :empresaId")
    Optional<Usuario> buscarNaEmpresa(@Param("id") UUID id, @Param("empresaId") UUID empresaId);

    @Query("select count(u) > 0 from Usuario u where lower(u.email) = :email")
    boolean existeEmailNormalizado(@Param("email") String email);

    @Query("select count(u) > 0 from Usuario u where lower(u.email) = :email and u.id <> :id")
    boolean existeEmailNormalizadoEmOutro(@Param("email") String email, @Param("id") UUID id);

    boolean existsByPapel(Papel papel);

    boolean existsByIdIn(List<UUID> ids);

    boolean existsByEmpresaId(UUID empresaId);

    boolean existsByEmpresaIdAndPapel(UUID empresaId, Papel papel);
}
