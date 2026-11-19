package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pontuacao_integrante")
@Getter
@Setter
@NoArgsConstructor
public class PontuacaoIntegrante {

    @Id
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @OneToOne
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    @OneToOne
    @JoinColumn(name = "empresa_id", insertable = false, updatable = false)
    private Empresa empresa;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "xp_total", nullable = false)
    private int xpTotal;

    @Column(name = "pontuacao_atingida_em")
    private OffsetDateTime pontuacaoAtingidaEm;
}
