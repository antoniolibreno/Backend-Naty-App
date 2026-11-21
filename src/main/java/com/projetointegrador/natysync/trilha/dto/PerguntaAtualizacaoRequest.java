package com.projetointegrador.natysync.trilha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PerguntaAtualizacaoRequest(
        @NotBlank String enunciado, @NotNull Integer ordem) {}
