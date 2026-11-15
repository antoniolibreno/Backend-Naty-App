package com.projetointegrador.natysync.gamificacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sequencia_integrante")
@Getter
@Setter
@NoArgsConstructor
public class SequenciaIntegrante {

    @Id
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "dias_atuais", nullable = false)
    private int diasAtuais;

    @Column(name = "maior_sequencia", nullable = false)
    private int maiorSequencia;

    @Column(name = "ultimo_evento_em")
    private OffsetDateTime ultimoEventoEm;

    @Column(name = "ultimo_dia_local")
    private LocalDate ultimoDiaLocal;
}
