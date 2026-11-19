package com.projetointegrador.natysync.shared.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Corpo de erro. Ramifique pelo codigo")
public record ErroResposta(
        @Schema(description = "Instante do erro", example = "2026-11-11T14:32:10.123-03:00")
        OffsetDateTime momento,

        @Schema(description = "Status HTTP", example = "409")
        int status,

        @Schema(description = "Código estável do erro", example = "EMAIL_JA_CADASTRADO")
        String codigo,

        @Schema(description = "Mensagem legível, pode variar", example = "Já existe uma conta com este e-mail")
        String mensagem,

        @Schema(description = "Caminho da requisição", example = "/api/v1/painel/integrantes")
        String caminho,

        @Schema(description = "Só em FALHA_DE_VALIDACAO") List<ErroCampo> campos) {

    @Schema(description = "Erro de validação de um campo")
    public record ErroCampo(
            @Schema(description = "Campo inválido", example = "email")
            String campo,

            @Schema(description = "Motivo da recusa", example = "deve ser um endereço de e-mail válido")
            String mensagem) {}
}
