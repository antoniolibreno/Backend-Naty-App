package com.projetointegrador.natysync.usuario.dto;

import com.projetointegrador.natysync.usuario.Papel;
import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, Papel papel) {}
