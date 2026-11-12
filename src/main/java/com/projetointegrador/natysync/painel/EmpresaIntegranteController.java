package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.painel.dto.AtivoRequest;
import com.projetointegrador.natysync.painel.dto.IntegranteAlteracaoRequest;
import com.projetointegrador.natysync.painel.dto.IntegranteCriacaoRequest;
import com.projetointegrador.natysync.painel.dto.IntegranteFiltro;
import com.projetointegrador.natysync.painel.dto.IntegranteResponse;
import com.projetointegrador.natysync.painel.dto.SenhaRequest;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/painel/empresas/{empresaId}/integrantes")
@Tag(
        name = "Painel: integrantes por empresa",
        description = "Integrantes de qualquer empresa escolhida na URL, restrito ao papel NATY.")
public class EmpresaIntegranteController {

    private final IntegranteService integranteService;

    public EmpresaIntegranteController(IntegranteService integranteService) {
        this.integranteService = integranteService;
    }

    @GetMapping
    @Operation(
            summary = "Listar os integrantes",
            description =
                    "Exige NATY. Paginada, no máximo 100 por página, ordenada por nome. Filtra por texto e por situação.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de integrantes"),
        @ApiResponse(
                responseCode = "400",
                description = "Parâmetro de paginação ou de filtro inválido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel diferente de NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa não encontrada",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PaginaResponse<IntegranteResponse> listar(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Parameter(description = "Texto buscado em nome e e-mail", example = "ana") @RequestParam(required = false)
                    String busca,
            @Parameter(description = "Situação da conta. Ausente traz todas", example = "true")
                    @RequestParam(required = false)
                    Boolean ativo,
            @ParameterObject Pageable pagina) {
        return integranteService.listar(empresaId, new IntegranteFiltro(busca, ativo), pagina);
    }

    @GetMapping("/{integranteId}")
    @Operation(summary = "Buscar um integrante", description = "Exige NATY. Integrante de outra empresa recebe 404.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Integrante encontrado"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel diferente de NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa ou integrante não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public IntegranteResponse buscar(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Parameter(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
                    @PathVariable
                    UUID integranteId) {
        return integranteService.buscar(empresaId, integranteId);
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar um integrante",
            description =
                    "Exige NATY. O e-mail é único no sistema, o papel é INTEGRANTE ou ADMIN e a senha tem de 8 caracteres a 72 bytes.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Integrante criado"),
        @ApiResponse(
                responseCode = "400",
                description = "Dado de cadastro ausente ou inválido",
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
                description = "E-mail já cadastrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<IntegranteResponse> criar(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Valid @RequestBody IntegranteCriacaoRequest requisicao) {
        IntegranteResponse resposta = integranteService.criar(empresaId, requisicao);
        return ResponseEntity.created(
                        URI.create("/api/v1/painel/empresas/" + empresaId + "/integrantes/" + resposta.id()))
                .body(resposta);
    }

    @PutMapping("/{integranteId}")
    @Operation(
            summary = "Alterar um integrante",
            description = "Exige NATY. Altera contas INTEGRANTE e ADMIN, e conta NATY recebe 409.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Integrante alterado"),
        @ApiResponse(
                responseCode = "400",
                description = "Dado de cadastro ausente ou inválido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel diferente de NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa ou integrante não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "E-mail em uso ou conta NATY como alvo",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public IntegranteResponse alterar(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Parameter(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
                    @PathVariable
                    UUID integranteId,
            @Valid @RequestBody IntegranteAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return integranteService.alterar(empresaId, integranteId, requisicao, quem);
    }

    @PutMapping("/{integranteId}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Definir a senha",
            description = "Exige NATY. Revoga as sessões do integrante. Senha de 8 caracteres a 72 bytes.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Senha definida"),
        @ApiResponse(
                responseCode = "400",
                description = "Senha ausente ou fora do limite",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel diferente de NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa ou integrante não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Conta NATY como alvo",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void definirSenha(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Parameter(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
                    @PathVariable
                    UUID integranteId,
            @Valid @RequestBody SenhaRequest requisicao,
            IntegranteDaRequisicao quem) {
        integranteService.definirSenha(empresaId, integranteId, requisicao.senha(), quem);
    }

    @PutMapping("/{integranteId}/ativo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Ativar ou desativar um integrante",
            description = "Exige NATY. Desativar revoga as sessões e preserva o progresso.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Situação alterada"),
        @ApiResponse(
                responseCode = "400",
                description = "Campo ativo ausente",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel diferente de NATY",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa ou integrante não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Conta NATY como alvo",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void definirAtivo(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Parameter(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
                    @PathVariable
                    UUID integranteId,
            @Valid @RequestBody AtivoRequest requisicao,
            IntegranteDaRequisicao quem) {
        integranteService.definirAtivo(empresaId, integranteId, requisicao.ativo(), quem);
    }
}
