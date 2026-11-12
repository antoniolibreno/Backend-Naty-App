package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.painel.dto.EmpresaRequest;
import com.projetointegrador.natysync.painel.dto.EmpresaResponse;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/painel/empresas")
@Tag(
        name = "Painel: empresas",
        description =
                "Cadastro de empresas. Todas as rotas exigem o papel NATY. A empresa e a raiz do isolamento de dados")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    @Operation(
            summary = "Lista as empresas",
            description =
                    "Exige NATY. Paginada por pagina e tamanho, com tamanho maximo de 100. A ordem e sempre por nome,"
                            + " e o parametro de ordenacao e ignorado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagina de empresas ordenada por nome"),
        @ApiResponse(
                responseCode = "400",
                description = "Parametro de consulta invalido. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel sem permissao para a rota. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PaginaResponse<EmpresaResponse> listar(@ParameterObject Pageable pagina) {
        return empresaService.listar(pagina);
    }

    @GetMapping("/{empresaId}")
    @Operation(
            summary = "Busca uma empresa pelo identificador",
            description = "Exige NATY. Devolve os dados cadastrais da empresa.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel sem permissao para a rota. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public EmpresaResponse buscarPorId(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId) {
        return empresaService.buscarPorId(empresaId);
    }

    @PostMapping
    @Operation(
            summary = "Cria uma empresa",
            description = "Exige NATY. O fuso horario e um identificador de regiao, como America/Sao_Paulo, e define o"
                    + " dia de estudo dos integrantes. Responde 201 com o cabecalho Location da empresa criada.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Empresa criada, com Location da empresa"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel sem permissao para a rota. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<EmpresaResponse> criar(@Valid @RequestBody EmpresaRequest requisicao) {
        EmpresaResponse resposta = empresaService.criar(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/painel/empresas/" + resposta.id()))
                .body(resposta);
    }

    @PutMapping("/{empresaId}")
    @Operation(
            summary = "Atualiza uma empresa",
            description = "Exige NATY. Substitui nome, situacao e fuso. Desativar a empresa revoga as sessoes dos"
                    + " integrantes dela. Empresa com conta NATY nao e desativada e devolve 409"
                    + " EMPRESA_COM_CONTA_NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa atualizada"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel sem permissao para a rota. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Empresa com conta NATY nao pode ser desativada. Codigo EMPRESA_COM_CONTA_NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public EmpresaResponse atualizar(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Valid @RequestBody EmpresaRequest requisicao) {
        return empresaService.atualizar(empresaId, requisicao);
    }

    @DeleteMapping("/{empresaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Exclui uma empresa sem integrantes",
            description = "Exige NATY. Empresa com integrante devolve 409 EMPRESA_COM_VINCULOS, e empresa com conta"
                    + " NATY devolve 409 EMPRESA_COM_CONTA_NATY. O caminho para empresa com historico e a desativacao.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Empresa excluida, sem corpo"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel sem permissao para a rota. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description =
                        "Empresa com integrante vinculado nao pode ser excluida. Codigo EMPRESA_COM_VINCULOS ou EMPRESA_COM_CONTA_NATY quando a empresa tem conta NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void excluir(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId) {
        empresaService.excluir(empresaId);
    }
}
