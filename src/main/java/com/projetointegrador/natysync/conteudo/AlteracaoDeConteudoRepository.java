package com.projetointegrador.natysync.conteudo;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlteracaoDeConteudoRepository extends JpaRepository<AlteracaoDeConteudo, UUID> {

    Page<AlteracaoDeConteudo> findByEntidadeId(UUID entidadeId, Pageable pagina);
}
