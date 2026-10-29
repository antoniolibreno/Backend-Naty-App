package com.projetointegrador.natysync.gamificacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conquista")
@Getter
@NoArgsConstructor
public class Conquista {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String codigo;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CriterioConquista criterio;

    @Column(nullable = false)
    private Integer limiar;

    @Column(nullable = false, unique = true)
    private Integer ordem;

    public boolean atingidaPor(IndicadoresDoIntegrante indicadores) {
        return criterio.indicador(indicadores) >= limiar;
    }
}
