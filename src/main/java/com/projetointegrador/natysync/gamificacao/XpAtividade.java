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
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "xp_atividade")
@Getter
@Setter
@NoArgsConstructor
public class XpAtividade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "atividade_id", nullable = false)
    private UUID atividadeId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(nullable = false)
    private int xp;

    @CreationTimestamp
    @Column(name = "concedido_em", nullable = false, updatable = false)
    private OffsetDateTime concedidoEm;
}
