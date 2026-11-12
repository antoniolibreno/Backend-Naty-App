package com.projetointegrador.natysync.acompanhamento;

import com.projetointegrador.natysync.progresso.ProgressoAtividade;
import com.projetointegrador.natysync.usuario.Papel;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ConclusoesRepository extends Repository<ProgressoAtividade, UUID> {

    @Query("""
            select new com.projetointegrador.natysync.acompanhamento.Conclusao(
                p.usuario.id, p.atividade.id, p.concluidoEm)
            from ProgressoAtividade p
            where p.usuario.id in :usuarioIds and p.concluidoEm is not null
            """)
    List<Conclusao> conclusoesDe(@Param("usuarioIds") Collection<UUID> usuarioIds);

    @Query("""
            select new com.projetointegrador.natysync.acompanhamento.ContagemPorAtividade(p.atividade.id, count(p))
            from ProgressoAtividade p
            where p.concluidoEm is not null and p.usuario.ativo = true and p.usuario.papel <> :papel
            group by p.atividade.id
            """)
    List<ContagemPorAtividade> contarConclusoesDeTodasAsEmpresas(@Param("papel") Papel papel);

    @Query("""
            select new com.projetointegrador.natysync.acompanhamento.ContagemPorAtividade(p.atividade.id, count(p))
            from ProgressoAtividade p
            where p.concluidoEm is not null and p.usuario.ativo = true and p.usuario.papel <> :papel
              and p.usuario.empresa.id = :empresaId
            group by p.atividade.id
            """)
    List<ContagemPorAtividade> contarConclusoesDaEmpresa(
            @Param("empresaId") UUID empresaId, @Param("papel") Papel papel);
}
