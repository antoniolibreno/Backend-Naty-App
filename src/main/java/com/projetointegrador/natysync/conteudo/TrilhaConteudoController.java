package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AtivacaoRequest;
import com.projetointegrador.natysync.conteudo.dto.OrdemRequest;
import com.projetointegrador.natysync.conteudo.dto.TrilhaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.TrilhaRequest;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trilhas")
@Tag(
        name = "Conteudo",
        description =
                "Escrita do conteudo global de treinamento: trilha, modulo, atividade, quiz, pergunta e alternativa."
                        + " Toda rota de escrita exige conta NATY. A ordem de criacao acrescenta ao fim, e conteudo com historico nao e removido."
                        + " Cada alteracao grava um registro de auditoria.")
public class TrilhaConteudoController {

    private final TrilhaConteudoService trilhaService;
    private final ReordenacaoService reordenacaoService;

    public TrilhaConteudoController(TrilhaConteudoService trilhaService, ReordenacaoService reordenacaoService) {
        this.trilhaService = trilhaService;
        this.reordenacaoService = reordenacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria uma trilha ativa no fim da ordem",
            description = "Cria a trilha ativa com ordem igual ao maior valor atual mais 1. Exige conta "
                    + "NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Trilha criada"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public TrilhaAdminResponse criar(@Valid @RequestBody TrilhaRequest requisicao, IntegranteDaRequisicao quem) {
        return trilhaService.criar(requisicao, quem.usuarioId());
    }

    @PutMapping("/{trilhaId}")
    @Operation(
            summary = "Altera titulo e descricao da trilha",
            description = "Substitui titulo e descricao. A ordem e a ativacao nao mudam. Exige conta NATY "
                    + "e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trilha alterada"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Trilha nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public TrilhaAdminResponse alterar(
            @Parameter(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
                    @PathVariable
                    UUID trilhaId,
            @Valid @RequestBody TrilhaRequest requisicao,
            IntegranteDaRequisicao quem) {
        return trilhaService.alterar(trilhaId, requisicao, quem.usuarioId());
    }

    @PutMapping("/{trilhaId}/ativa")
    @Operation(
            summary = "Ativa ou desativa a trilha",
            description = "Trilha inativa sai da listagem, mas continua legivel por identificador e "
                    + "mantem o progresso, a tentativa e o ponto de quem a estudou. E a alternativa a "
                    + "remover trilha com historico. Exige conta NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ativacao gravada"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Trilha nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public TrilhaAdminResponse definirAtivacao(
            @Parameter(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
                    @PathVariable
                    UUID trilhaId,
            @Valid @RequestBody AtivacaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return trilhaService.definirAtivacao(trilhaId, requisicao.ativa(), quem.usuarioId());
    }

    @DeleteMapping("/{trilhaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove a trilha sem historico",
            description = "Remove a trilha com seus modulos, atividades, quiz, perguntas e alternativas. "
                    + "Recusa quando alguma atividade tem progresso, tentativa ou ponto registrado, e "
                    + "a alternativa e desativar a trilha. Exige conta NATY e vale para todas as "
                    + "empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Trilha removida"),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Trilha nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Alguma atividade da trilha tem historico. Codigo CONTEUDO_COM_PROGRESSO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void remover(
            @Parameter(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
                    @PathVariable
                    UUID trilhaId,
            IntegranteDaRequisicao quem) {
        trilhaService.remover(trilhaId, quem.usuarioId());
    }

    @PutMapping("/{trilhaId}/modulos/ordem")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Reordena os modulos da trilha",
            description = "Recebe todos os identificadores dos modulos da trilha na ordem desejada e "
                    + "renumera de 1 a n. Lista diferente dos modulos atuais, ou com repeticao, e "
                    + "recusada. Registra REORDENACAO na trilha. Exige conta NATY e vale para todas "
                    + "as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Modulos reordenados"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Trilha nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "A lista nao contem exatamente os modulos atuais da trilha. Codigo " + "ORDEM_INVALIDA",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void reordenarModulos(
            @Parameter(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
                    @PathVariable
                    UUID trilhaId,
            @Valid @RequestBody OrdemRequest requisicao,
            IntegranteDaRequisicao quem) {
        reordenacaoService.reordenarModulos(trilhaId, requisicao.ids(), quem.usuarioId());
    }
}
