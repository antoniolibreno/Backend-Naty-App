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
@Tag(name = "Acompanhamento", description = "Progresso dos integrantes de qualquer empresa. Exige a conta NATY.")
public class AcompanhamentoController {

    private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final AcompanhamentoService service;

    public AcompanhamentoController(AcompanhamentoService service) {
        this.service = service;
    }

    @GetMapping("/empresas/{empresaId}/integrantes")
    @Operation(
            summary = "Listar os integrantes da empresa",
            description = "Devolve a situação de cada integrante ativo, com filtro opcional por situação. Parado é"
                    + " quem passou o prazo de dias sem concluir atividade. Paginada, com até 100 itens.")
    @ApiResponse(responseCode = "200", description = "Página de integrantes")
    @ApiResponse(
            responseCode = "400",
            description = "Situação desconhecida ou identificador malformado",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "403",
            description = "Apenas a conta NATY acessa",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Empresa não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public PaginaResponse<IntegranteAcompanhadoResponse> listar(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Parameter(description = "Filtra por situação", example = "PARADO") @RequestParam(required = false)
                    SituacaoDeAcompanhamento situacao,
            @ParameterObject Pageable pagina) {
        return service.listar(empresaId, situacao, pagina);
    }

    @GetMapping("/empresas/{empresaId}/metricas")
    @Operation(
            summary = "Buscar as métricas da empresa",
            description = "Devolve adesão, percentual médio de conclusão e a contagem de integrantes por situação.")
    @ApiResponse(responseCode = "200", description = "Métricas da empresa")
    @ApiResponse(
            responseCode = "400",
            description = "Identificador malformado",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "403",
            description = "Apenas a conta NATY acessa",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Empresa não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public MetricasDaEmpresaResponse metricas(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId) {
        return service.metricas(empresaId);
    }

    @GetMapping("/empresas/{empresaId}/integrantes.csv")
    @Operation(
            summary = "Exportar os integrantes em CSV",
            description = "Devolve a lista de integrantes da empresa como arquivo CSV, sem paginação, com o mesmo"
                    + " filtro de situação da listagem.")
    @ApiResponse(
            responseCode = "200",
            description = "Arquivo CSV",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string")))
    @ApiResponse(
            responseCode = "400",
            description = "Situação desconhecida ou identificador malformado",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "403",
            description = "Apenas a conta NATY acessa",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Empresa não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public ResponseEntity<String> exportar(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Parameter(description = "Filtra por situação", example = "PARADO") @RequestParam(required = false)
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
            summary = "Listar o funil por atividade",
            description = "Devolve quantos integrantes chegaram e quantos concluíram cada atividade ativa."
                    + " Sem filtro de empresa, soma todas.")
    @ApiResponse(responseCode = "200", description = "Funil das atividades")
    @ApiResponse(
            responseCode = "400",
            description = "Identificador malformado",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "403",
            description = "Apenas a conta NATY acessa",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Empresa do filtro não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public List<FunilDeAtividadeResponse> funil(
            @Parameter(
                            description = "Filtra por empresa. Sem filtro, soma todas",
                            example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @RequestParam(required = false)
                    UUID empresaId) {
        return service.funil(empresaId);
    }
}
