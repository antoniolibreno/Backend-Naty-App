package com.projetointegrador.natysync.conteudo.dto;

import java.util.List;
import java.util.UUID;

public record PerguntaAdminResponse(
        UUID id, String enunciado, Integer ordem, List<AlternativaAdminResponse> alternativas) {}
