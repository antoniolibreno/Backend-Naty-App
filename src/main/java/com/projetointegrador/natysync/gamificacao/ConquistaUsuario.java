package com.projetointegrador.natysync.gamificacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "conquista_usuario")
@IdClass(ConquistaUsuarioId.class)
@Getter
@Setter
@NoArgsConstructor
public class ConquistaUsuario {

    @Id
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Id
    @Column(name = "conquista_id")
    private UUID conquistaId;

    @CreationTimestamp
    @Column(name = "concedida_em", nullable = false, updatable = false)
    private OffsetDateTime concedidaEm;
}
