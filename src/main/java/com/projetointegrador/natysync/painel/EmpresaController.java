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
@Tag(name = "Painel: empresas", description = "Cadastro de empresas, restrito ao papel NATY.")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    @Operation(
            summary = "Listar as empresas",
            description = "Exige NATY. Paginada, no máximo 100 por página, ordenada por nome.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de empresas"),
        @ApiResponse(
                responseCode = "400",
                description = "Parâmetro de paginação ou de filtro inválido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel diferente de NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PaginaResponse<EmpresaResponse> listar(@ParameterObject Pageable pagina) {
        return empresaService.listar(pagina);
    }

    @GetMapping("/{empresaId}")
    @Operation(summary = "Buscar uma empresa", description = "Exige NATY. Devolve os dados cadastrais da empresa.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel diferente de NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa não encontrada",
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
            summary = "Criar uma empresa",
            description = "Exige NATY. O fuso horário, como America/Sao_Paulo, define o dia de estudo dos integrantes.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Empresa criada"),
        @ApiResponse(
                responseCode = "400",
                description = "Dado da empresa ausente ou inválido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel diferente de NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<EmpresaResponse> criar(@Valid @RequestBody EmpresaRequest requisicao) {
        EmpresaResponse resposta = empresaService.criar(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/painel/empresas/" + resposta.id()))
                .body(resposta);
    }

    @PutMapping("/{empresaId}")
    @Operation(
            summary = "Atualizar uma empresa",
            description =
                    "Exige NATY. Substitui nome, situação e fuso. Desativar revoga as sessões dos integrantes, e empresa com conta NATY não é desativada.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empresa atualizada"),
        @ApiResponse(
                responseCode = "400",
                description = "Dado da empresa ausente ou inválido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel diferente de NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa não encontrada",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Empresa com conta NATY não pode ser desativada",
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
            summary = "Excluir uma empresa",
            description = "Exige NATY. Só exclui empresa sem integrante e sem conta NATY. Com histórico, desative.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Empresa excluída"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel diferente de NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa não encontrada",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Empresa com integrante vinculado ou com conta NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void excluir(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId) {
        empresaService.excluir(empresaId);
    }
}
