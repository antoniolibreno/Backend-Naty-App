package com.projetointegrador.natysync.conteudo;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditoriaDeConteudo {

    private final AlteracaoDeConteudoRepository repository;

    public AuditoriaDeConteudo(AlteracaoDeConteudoRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(UUID usuarioId, EntidadeDeConteudo entidade, UUID entidadeId, AcaoDeConteudo acao) {
        AlteracaoDeConteudo alteracao = new AlteracaoDeConteudo();
        alteracao.setUsuarioId(usuarioId);
        alteracao.setEntidade(entidade);
        alteracao.setEntidadeId(entidadeId);
        alteracao.setAcao(acao);
        alteracao.setOcorridaEm(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS));
        repository.save(alteracao);
    }
}
