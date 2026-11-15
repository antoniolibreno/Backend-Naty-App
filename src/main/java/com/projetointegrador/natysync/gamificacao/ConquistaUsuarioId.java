package com.projetointegrador.natysync.gamificacao;

import java.io.Serializable;
import java.util.UUID;

public record ConquistaUsuarioId(UUID usuarioId, UUID conquistaId) implements Serializable {}
