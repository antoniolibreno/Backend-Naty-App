package com.projetointegrador.natysync.trilha.dto;

import java.util.List;
import java.util.UUID;

public record PerguntaResponse(UUID id, String enunciado, Integer ordem, List<AlternativaResponse> alternativas) {}
