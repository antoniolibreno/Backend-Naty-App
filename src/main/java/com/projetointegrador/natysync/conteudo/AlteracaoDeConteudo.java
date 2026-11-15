package com.projetointegrador.natysync.conteudo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alteracao_conteudo")
@Getter
@Setter
@NoArgsConstructor
public class AlteracaoDeConteudo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntidadeDeConteudo entidade;

    @Column(name = "entidade_id", nullable = false)
    private UUID entidadeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcaoDeConteudo acao;

    @Column(name = "ocorrida_em", nullable = false)
    private OffsetDateTime ocorridaEm;
}
