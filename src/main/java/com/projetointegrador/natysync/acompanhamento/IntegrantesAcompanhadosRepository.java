package com.projetointegrador.natysync.acompanhamento;

import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.Usuario;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface IntegrantesAcompanhadosRepository extends Repository<Usuario, UUID> {

    List<Usuario> findByEmpresaIdAndAtivoTrueAndPapelNot(UUID empresaId, Papel papel);

    @Query("select count(u) from Usuario u where u.ativo = true and u.papel <> :papel")
    long contarConsideradosDeTodasAsEmpresas(@Param("papel") Papel papel);

    @Query("select count(u) from Usuario u where u.ativo = true and u.papel <> :papel and u.empresa.id = :empresaId")
    long contarConsideradosDaEmpresa(@Param("empresaId") UUID empresaId, @Param("papel") Papel papel);
}
