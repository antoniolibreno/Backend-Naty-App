package com.projetointegrador.natysync.gamificacao;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PontuacaoIntegranteRepository extends JpaRepository<PontuacaoIntegrante, UUID> {

    interface RankingLinha {
        UUID getIntegranteId();

        String getNome();

        Integer getXpTotal();

        OffsetDateTime getPontuacaoAtingidaEm();

        Integer getSequenciaAtual();
    }

    @Query("""
        select u.id as integranteId, u.nome as nome,
               coalesce(p.xpTotal, 0) as xpTotal,
               p.pontuacaoAtingidaEm as pontuacaoAtingidaEm,
               coalesce(s.diasAtuais, 0) as sequenciaAtual
        from Usuario u
        left join PontuacaoIntegrante p on p.usuarioId = u.id
        left join SequenciaIntegrante s on s.usuarioId = u.id
        where u.empresa.id = :empresaId and u.papel = com.projetointegrador.natysync.usuario.Papel.INTEGRANTE
          and u.ativo = true
        order by coalesce(p.xpTotal, 0) desc, p.pontuacaoAtingidaEm asc nulls last, u.nome asc
        """)
    Page<RankingLinha> rankingDaEmpresa(@Param("empresaId") UUID empresaId, Pageable pageable);
}
