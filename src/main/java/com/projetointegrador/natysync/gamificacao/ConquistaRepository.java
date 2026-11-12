package com.projetointegrador.natysync.gamificacao;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConquistaRepository extends JpaRepository<Conquista, UUID> {

    List<Conquista> findAllByOrderByOrdemAsc();
}
