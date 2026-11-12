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
        description = "Integrantes de qualquer empresa, escolhida pela URL. Todas as rotas exigem o papel NATY")
public class EmpresaIntegranteController {

    private final IntegranteService integranteService;

    public EmpresaIntegranteController(IntegranteService integranteService) {
        this.integranteService = integranteService;
    }

    @GetMapping
    @Operation(
            summary = "Lista os integrantes da empresa",
            description =
                    "Exige NATY. A empresa vem da URL. Paginada por pagina e tamanho, com tamanho maximo de 100. A ordem e"
                            + " sempre por nome e identificador, e o parametro de ordenacao e ignorado. O filtro busca compara"
                            + " com nome e e-mail, e o filtro ativo com a situacao da conta.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagina de integrantes ordenada por nome"),
        @ApiResponse(
                responseCode = "400",
                description = "Parametro de consulta invalido. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel sem permissao para a rota. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Empresa nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PaginaResponse<IntegranteResponse> listar(
            @Parameter(description = "Identificador da empresa", example = "3f6c1a52-8d4e-4b7a-9c21-5e0b7d4a1f10")
                    @PathVariable
                    UUID empresaId,
            @Parameter(description = "Texto comparado com nome e e-mail, sem diferenca de caixa", example = "ana")
                    @RequestParam(required = false)
                    String busca,
            @Parameter(description = "Filtra por situacao. Ausente devolve ativos e inativos", example = "true")
                    @RequestParam(required = false)
                    Boolean ativo,
            @ParameterObject Pageable pagina) {
        return integranteService.listar(empresaId, new IntegranteFiltro(busca, ativo), pagina);
    }

    @GetMapping("/{integranteId}")
    @Operation(
            summary = "Busca um integrante da empresa",
            description = "Exige NATY. A empresa vem da URL. Integrante de outra empresa responde 404, nunca 403.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Integrante encontrado"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel sem permissao para a rota. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Integrante ou empresa nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
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
            summary = "Cadastra um integrante na empresa",
            description = "Exige NATY. O e-mail e unico no sistema inteiro. O papel e INTEGRANTE ou ADMIN, e NATY e"
                    + " recusado. A senha tem de 8 caracteres a 72 bytes e nunca volta na resposta. Responde 201 com o"
                    + " cabecalho Location do integrante criado.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Integrante criado, com Location do integrante"),
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
                description = "Empresa nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "E-mail ja cadastrado no sistema. Codigo EMAIL_JA_CADASTRADO",
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
            summary = "Altera nome, e-mail, papel e perfil do integrante",
            description = "Exige NATY. NATY altera contas INTEGRANTE e ADMIN. Conta NATY"
                    + " nao e administrada pelo painel. Perfil ausente mantem o perfil atual. O e-mail continua unico no"
                    + " sistema inteiro.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Integrante alterado"),
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
                description = "Integrante ou empresa nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description =
                        "E-mail ja cadastrado no sistema, codigo EMAIL_JA_CADASTRADO, ou conta NATY como alvo, codigo CONTA_NATY_FORA_DO_PAINEL",
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
            summary = "Define a senha do integrante",
            description = "Exige NATY. Revoga todas as sessoes do integrante. Conta ADMIN so tem a senha definida pelo"
                    + " papel NATY, e ninguem redefine a propria senha pelo painel. A senha tem de 8 caracteres a 72"
                    + " bytes.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Senha definida e sessoes revogadas, sem corpo"),
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
                description = "Integrante ou empresa nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Conta NATY nao e administrada pelo painel. Codigo CONTA_NATY_FORA_DO_PAINEL",
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
            summary = "Ativa ou desativa o integrante",
            description = "Exige NATY. Desativar revoga as sessoes e preserva o progresso. Integrante nunca e"
                    + " apagado. Conta ADMIN so e ativada ou desativada pelo papel NATY, e ninguem desativa a propria"
                    + " conta pelo painel.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Situacao alterada, sem corpo"),
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
                description = "Integrante ou empresa nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Conta NATY nao e administrada pelo painel. Codigo CONTA_NATY_FORA_DO_PAINEL",
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
