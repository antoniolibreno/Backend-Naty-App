package com.projetointegrador.natysync.empresa;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    @Query("""
            select e from Empresa e
            where lower(e.nome) like :texto escape '\\'
               or lower(e.razaoSocial) like :texto escape '\\'
               or e.cnpj like coalesce(:cnpjParcial, '') escape '\\'
            """)
    Page<Empresa> buscar(@Param("texto") String texto, @Param("cnpjParcial") String cnpjParcial, Pageable pagina);
}
