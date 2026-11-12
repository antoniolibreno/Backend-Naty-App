package com.projetointegrador.natysync.conteudo.dto;

import java.util.UUID;

public record AlternativaAdminResponse(UUID id, String texto, boolean correta, Integer ordem) {}
