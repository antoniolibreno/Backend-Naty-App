package com.projetointegrador.natysync.painel.dto;

import com.projetointegrador.natysync.usuario.Papel;
import java.time.OffsetDateTime;
import java.util.UUID;

public record IntegranteResponse(
        UUID id,
        UUID empresaId,
        String nome,
        String email,
        Papel papel,
        String perfil,
        boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {}
