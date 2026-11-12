package com.projetointegrador.natysync.shared.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Corpo unico de erro da API. O app ramifica pelo campo codigo, nunca pela mensagem.")
public record ErroResposta(
        @Schema(description = "Instante em que o erro foi gerado", example = "2026-11-11T14:32:10.123-03:00")
        OffsetDateTime momento,

        @Schema(description = "Status HTTP da resposta", example = "409")
        int status,

        @Schema(description = "Codigo estavel do erro, em maiusculas com sublinhado", example = "EMAIL_JA_CADASTRADO")
        String codigo,

        @Schema(
                description = "Mensagem para leitura humana. Pode mudar de texto, o codigo nao muda",
                example = "Ja existe uma conta com este e-mail")
        String mensagem,

        @Schema(description = "Caminho da requisicao que gerou o erro", example = "/api/v1/painel/integrantes")
        String caminho,

        @Schema(description = "Erros por campo. Vem preenchida so em FALHA_DE_VALIDACAO e vazia nos demais codigos")
        List<ErroCampo> campos) {

    @Schema(description = "Violacao de validacao de um campo da requisicao")
    public record ErroCampo(
            @Schema(description = "Nome do campo invalido", example = "email")
            String campo,

            @Schema(description = "Motivo da recusa", example = "deve ser um endereco de e-mail valido")
            String mensagem) {}
}
