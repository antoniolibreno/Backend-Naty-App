package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record OrdemRequest(@NotEmpty List<UUID> ids) {}
