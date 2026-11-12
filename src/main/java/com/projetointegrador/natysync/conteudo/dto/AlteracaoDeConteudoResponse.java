package com.projetointegrador.natysync.conteudo.dto;

import com.projetointegrador.natysync.conteudo.AcaoDeConteudo;
import com.projetointegrador.natysync.conteudo.EntidadeDeConteudo;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlteracaoDeConteudoResponse(
        UUID id,
        UUID usuarioId,
        EntidadeDeConteudo entidade,
        UUID entidadeId,
        AcaoDeConteudo acao,
        OffsetDateTime ocorridaEm) {}
