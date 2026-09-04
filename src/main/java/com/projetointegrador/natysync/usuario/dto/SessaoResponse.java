package com.projetointegrador.natysync.usuario.dto;

import java.util.UUID;

public record SessaoResponse(UUID usuarioId, UUID empresaId, String nome, String email) {}
