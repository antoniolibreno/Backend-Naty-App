package com.projetointegrador.natysync.trilha;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtividadeRepository extends JpaRepository<Atividade, UUID> {}
