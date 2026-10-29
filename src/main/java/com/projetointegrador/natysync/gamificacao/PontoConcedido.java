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
@Table(name = "ponto_concedido")
@Getter
@NoArgsConstructor
public class PontoConcedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "atividade_id", nullable = false)
    private UUID atividadeId;

    @Column(nullable = false)
    private Integer xp;

    @Column(name = "concedido_em", nullable = false)
    private OffsetDateTime concedidoEm;
}
