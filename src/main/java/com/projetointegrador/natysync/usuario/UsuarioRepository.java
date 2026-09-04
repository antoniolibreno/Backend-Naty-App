package com.projetointegrador.natysync.usuario;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    @Query("select u from Usuario u join fetch u.empresa where lower(u.email) = :email")
    Optional<Usuario> buscarPorEmailNormalizado(@Param("email") String email);
}
