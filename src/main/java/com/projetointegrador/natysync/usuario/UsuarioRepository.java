package com.projetointegrador.natysync.usuario;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID>, JpaSpecificationExecutor<Usuario> {

    @Query("select u from Usuario u join fetch u.empresa where lower(u.email) = :email")
    Optional<Usuario> buscarPorEmailNormalizado(@Param("email") String email);

    @Query("select u from Usuario u join fetch u.empresa where u.id = :id")
    Optional<Usuario> buscarComEmpresaPorId(@Param("id") UUID id);

    @Query("select u from Usuario u join fetch u.empresa where u.id = :id and u.empresa.id = :empresaId")
    Optional<Usuario> buscarNaEmpresa(@Param("id") UUID id, @Param("empresaId") UUID empresaId);

    @Query("select count(u) > 0 from Usuario u where lower(u.email) = :email")
    boolean existeEmailNormalizado(@Param("email") String email);

    @Query("select count(u) > 0 from Usuario u where lower(u.email) = :email and u.id <> :id")
    boolean existeEmailNormalizadoEmOutro(@Param("email") String email, @Param("id") UUID id);

    boolean existsByPapel(Papel papel);

    boolean existsByEmpresaId(UUID empresaId);
}
