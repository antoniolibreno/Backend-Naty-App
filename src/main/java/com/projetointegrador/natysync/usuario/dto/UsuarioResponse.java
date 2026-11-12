package com.projetointegrador.natysync.usuario.dto;

import com.projetointegrador.natysync.usuario.Papel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Integrante ativo da empresa de quem chama. Nao expoe e-mail nem dados de cadastro")
public record UsuarioResponse(
        @Schema(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
        UUID id,

        @Schema(description = "Nome do integrante", example = "Ana Souza")
        String nome,

        @Schema(description = "Papel do integrante no sistema", example = "INTEGRANTE")
        Papel papel) {}
