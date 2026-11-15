package com.projetointegrador.natysync.gamificacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conquista_obtida")
@Getter
@NoArgsConstructor
public class ConquistaObtida {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "conquista_id", nullable = false)
    private UUID conquistaId;

    @Column(name = "obtida_em", nullable = false)
    private OffsetDateTime obtidaEm;
}
