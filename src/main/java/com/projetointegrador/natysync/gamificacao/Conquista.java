package com.projetointegrador.natysync.gamificacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conquista")
@Getter
@Setter
@NoArgsConstructor
public class Conquista {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String codigo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(name = "valor_alvo")
    private Integer valorAlvo;

    @Column(nullable = false)
    private boolean ativa;
}
