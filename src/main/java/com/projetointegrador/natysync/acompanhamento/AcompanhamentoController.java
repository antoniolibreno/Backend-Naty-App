package com.projetointegrador.natysync.acompanhamento;

import com.projetointegrador.natysync.acompanhamento.dto.FunilDeAtividadeResponse;
import com.projetointegrador.natysync.acompanhamento.dto.IntegranteAcompanhadoResponse;
import com.projetointegrador.natysync.acompanhamento.dto.MetricasDaEmpresaResponse;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/acompanhamento")
@Tag(
        name = "Acompanhamento",
        description =
                "Leitura do progresso dos integrantes de qualquer empresa, restrita a conta NATY. A empresa vem do"
                        + " caminho ou do filtro, nunca da identidade da requisicao. Conta NATY e integrante desativado ficam fora das"
                        + " contagens. Somente leitura")
public class AcompanhamentoController {

    private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final AcompanhamentoService service;

    public AcompanhamentoController(AcompanhamentoService service) {
        this.service = service;
    }

    @GetMapping("/empresas/{empresaId}/integrantes")
    @Operation(
            summary = "Lista os integrantes da empresa com a situacao de cada um",
            description = "Exige papel NATY. Considera integrantes ativos de papel diferente de NATY, sobre as trilhas"
                    + " ativas. Situacoes PARADO, SEM_INICIO, AVANCANDO e CONCLUIU, nessa ordem, com mais dias sem"
                    + " concluir primeiro e nome como desempate. O filtro situacao restringe a uma delas. Parado e o"
                    + " integrante que passou app.acompanhamento.dias-parado dias de calendario, no fuso da empresa,"
                    + " sem concluir atividade. Paginada por pagina e tamanho, com tamanho maximo de 100. Somente"
                    + " leitura.")
    @ApiResponse(responseCode = "200", description = "Pagina de integrantes com situacao, percentual e atividade atual")
    @ApiResponse(
            responseCode = "400",
            description =
                    "Parametro invalido, como situacao desconhecida ou identificador malformado. Codigo FALHA_DE_VALIDACAO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "403",
            description = "A conta nao tem papel NATY. Codigo ACESSO_NEGADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Empresa inexistente. Codigo RECURSO_NAO_ENCONTRADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public PaginaResponse<IntegranteAcompanhadoResponse> listar(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Parameter(
                            description = "Restringe a uma situacao: PARADO, SEM_INICIO, AVANCANDO ou CONCLUIU",
                            example = "PARADO")
                    @RequestParam(required = false)
                    SituacaoDeAcompanhamento situacao,
            @ParameterObject Pageable pagina) {
        return service.listar(empresaId, situacao, pagina);
    }

    @GetMapping("/empresas/{empresaId}/metricas")
    @Operation(
            summary = "Devolve adesao, percentual medio de conclusao e a contagem por situacao",
            description = "Exige papel NATY. Adesao e o percentual de integrantes com ao menos uma atividade"
                    + " concluida, e o percentual medio e a media do percentual concluido de cada um, ambos inteiros"
                    + " e arredondados para baixo. As contagens coincidem com as situacoes da listagem de"
                    + " integrantes. Somente leitura.")
    @ApiResponse(responseCode = "200", description = "Metricas da empresa")
    @ApiResponse(
            responseCode = "400",
            description = "Identificador da empresa malformado. Codigo FALHA_DE_VALIDACAO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "403",
            description = "A conta nao tem papel NATY. Codigo ACESSO_NEGADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Empresa inexistente. Codigo RECURSO_NAO_ENCONTRADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public MetricasDaEmpresaResponse metricas(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId) {
        return service.metricas(empresaId);
    }

    @GetMapping("/empresas/{empresaId}/integrantes.csv")
    @Operation(
            summary = "Exporta a lista de integrantes em CSV",
            description = "Exige papel NATY. Mesma ordem e mesmo filtro situacao da listagem, sem paginacao. Resposta"
                    + " text/csv em UTF-8 com BOM, RFC 4180 com separador virgula e fim de linha CRLF. Colunas:"
                    + " nome, email, papel, situacao, atividades_concluidas, total_atividades, percentual_concluido,"
                    + " atividade_atual, ultima_conclusao, dias_sem_concluir. Celula de texto que comeca com =, +, -"
                    + " ou @ vai com apostrofo no inicio, e celula com virgula, aspas ou quebra de linha vai entre"
                    + " aspas. O arquivo se chama integrantes-<empresaId>.csv e vem em Content-Disposition como"
                    + " anexo. Somente leitura.")
    @ApiResponse(
            responseCode = "200",
            description = "Arquivo CSV dos integrantes da empresa",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string")))
    @ApiResponse(
            responseCode = "400",
            description =
                    "Parametro invalido, como situacao desconhecida ou identificador malformado. Codigo FALHA_DE_VALIDACAO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "403",
            description = "A conta nao tem papel NATY. Codigo ACESSO_NEGADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Empresa inexistente. Codigo RECURSO_NAO_ENCONTRADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public ResponseEntity<String> exportar(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Parameter(
                            description = "Restringe a uma situacao: PARADO, SEM_INICIO, AVANCANDO ou CONCLUIU",
                            example = "PARADO")
                    @RequestParam(required = false)
                    SituacaoDeAcompanhamento situacao) {
        String csv = CsvDeAcompanhamento.gerar(service.todos(empresaId, situacao));
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("integrantes-" + empresaId + ".csv")
                                .build()
                                .toString())
                .body(csv);
    }

    @GetMapping("/atividades")
    @Operation(
            summary = "Mostra quantos chegaram e quantos passaram em cada atividade",
            description = "Exige papel NATY. Lista uma linha por atividade ativa, na ordem das trilhas, sem"
                    + " paginacao. Chegaram sao os integrantes para quem a atividade esta liberada: todos na primeira"
                    + " atividade de uma trilha e quem concluiu a anterior nas demais. Passaram sao os que a"
                    + " concluiram. O filtro empresaId restringe a uma empresa, e sem ele soma todas. Somente"
                    + " leitura.")
    @ApiResponse(responseCode = "200", description = "Funil de todas as atividades ativas")
    @ApiResponse(
            responseCode = "400",
            description = "Identificador da empresa malformado. Codigo FALHA_DE_VALIDACAO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "403",
            description = "A conta nao tem papel NATY. Codigo ACESSO_NEGADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Empresa informada no filtro inexistente. Codigo RECURSO_NAO_ENCONTRADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public List<FunilDeAtividadeResponse> funil(
            @Parameter(
                            description = "Restringe o funil a uma empresa. Sem o filtro, soma todas as empresas",
                            example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @RequestParam(required = false)
                    UUID empresaId) {
        return service.funil(empresaId);
    }
}
