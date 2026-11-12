package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.ModuloAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.ModuloCriacaoRequest;
import com.projetointegrador.natysync.conteudo.dto.OrdemRequest;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.trilha.dto.ModuloResponse;
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
@RequestMapping("/api/v1/modulos")
@Tag(
        name = "Conteudo",
        description =
                "Escrita do conteudo global de treinamento: trilha, modulo, atividade, quiz, pergunta e alternativa."
                        + " Toda rota de escrita exige conta NATY. A ordem de criacao acrescenta ao fim, e conteudo com historico nao e removido."
                        + " Cada alteracao grava um registro de auditoria.")
public class ModuloConteudoController {

    private final ModuloConteudoService moduloService;
    private final ReordenacaoService reordenacaoService;

    public ModuloConteudoController(ModuloConteudoService moduloService, ReordenacaoService reordenacaoService) {
        this.moduloService = moduloService;
        this.reordenacaoService = reordenacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria um modulo no fim da trilha",
            description = "Cria o modulo com ordem igual ao maior valor atual da trilha mais 1. Exige "
                    + "conta NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Modulo criado"),
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
    public ModuloResponse criar(@Valid @RequestBody ModuloCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return moduloService.criar(requisicao, quem.usuarioId());
    }

    @PutMapping("/{moduloId}")
    @Operation(
            summary = "Altera titulo e descricao do modulo",
            description = "Substitui titulo e descricao. A ordem nao muda. Exige conta NATY e vale para "
                    + "todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Modulo alterado"),
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
                description = "Modulo nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ModuloResponse alterar(
            @Parameter(description = "Identificador do modulo", example = "5d1f8a63-7e2c-4b90-a4d5-1f3b9c6e8a07")
                    @PathVariable
                    UUID moduloId,
            @Valid @RequestBody ModuloAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return moduloService.alterar(moduloId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{moduloId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove o modulo sem historico",
            description = "Remove o modulo com suas atividades, quiz, perguntas e alternativas. Recusa "
                    + "quando alguma atividade dele tem progresso, tentativa ou ponto registrado. "
                    + "Exige conta NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Modulo removido"),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Modulo nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Alguma atividade do modulo tem historico. Codigo CONTEUDO_COM_PROGRESSO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void remover(
            @Parameter(description = "Identificador do modulo", example = "5d1f8a63-7e2c-4b90-a4d5-1f3b9c6e8a07")
                    @PathVariable
                    UUID moduloId,
            IntegranteDaRequisicao quem) {
        moduloService.remover(moduloId, quem.usuarioId());
    }

    @PutMapping("/{moduloId}/atividades/ordem")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Reordena as atividades do modulo",
            description = "Recebe todos os identificadores das atividades do modulo na ordem desejada e "
                    + "renumera de 1 a n. Lista diferente das atividades atuais, ou com repeticao, e "
                    + "recusada. Registra REORDENACAO no modulo. Exige conta NATY e vale para todas "
                    + "as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Atividades reordenadas"),
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
                description = "Modulo nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description =
                        "A lista nao contem exatamente as atividades atuais do modulo. Codigo " + "ORDEM_INVALIDA",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void reordenarAtividades(
            @Parameter(description = "Identificador do modulo", example = "5d1f8a63-7e2c-4b90-a4d5-1f3b9c6e8a07")
                    @PathVariable
                    UUID moduloId,
            @Valid @RequestBody OrdemRequest requisicao,
            IntegranteDaRequisicao quem) {
        reordenacaoService.reordenarAtividades(moduloId, requisicao.ids(), quem.usuarioId());
    }
}
