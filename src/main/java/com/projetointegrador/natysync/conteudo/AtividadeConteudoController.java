package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AtividadeAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.AtividadeCriacaoRequest;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.trilha.dto.AtividadeDetalheResponse;
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
@RequestMapping("/api/v1/atividades")
@Tag(
        name = "Conteudo",
        description =
                "Escrita do conteudo global de treinamento: trilha, modulo, atividade, quiz, pergunta e alternativa."
                        + " Toda rota de escrita exige conta NATY. A ordem de criacao acrescenta ao fim, e conteudo com historico nao e removido."
                        + " Cada alteracao grava um registro de auditoria.")
public class AtividadeConteudoController {

    private final AtividadeConteudoService atividadeService;

    public AtividadeConteudoController(AtividadeConteudoService atividadeService) {
        this.atividadeService = atividadeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria uma atividade no fim do modulo",
            description = "Cria a atividade com ordem igual ao maior valor atual do modulo mais 1. Video "
                    + "deve ser URL HTTPS do YouTube com duracao positiva em segundos, e a duracao so "
                    + "existe com video. O XP vale 10 quando omitido. Sem quiz, a atividade conclui "
                    + "quando o video e marcado como assistido. Exige conta NATY e vale para todas as "
                    + "empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Atividade criada"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido, video fora do padrao HTTPS do YouTube ou duracao incoerente "
                        + "com o video. Codigo FALHA_DE_VALIDACAO",
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
    public AtividadeDetalheResponse criar(
            @Valid @RequestBody AtividadeCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return atividadeService.criar(requisicao, quem.usuarioId());
    }

    @PutMapping("/{atividadeId}")
    @Operation(
            summary = "Altera os dados da atividade",
            description = "Substitui titulo, descricao, imagem, video, duracao e XP. Video deve ser URL "
                    + "HTTPS do YouTube com duracao positiva em segundos, e a duracao so existe com "
                    + "video. O XP vale 10 quando omitido. O XP ja concedido fica congelado. Exige "
                    + "conta NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Atividade alterada"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido, video fora do padrao HTTPS do YouTube ou duracao incoerente "
                        + "com o video. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Atividade nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public AtividadeDetalheResponse alterar(
            @Parameter(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
                    @PathVariable
                    UUID atividadeId,
            @Valid @RequestBody AtividadeAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return atividadeService.alterar(atividadeId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{atividadeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove a atividade sem historico",
            description = "Remove a atividade com o quiz, as perguntas e as alternativas. Recusa quando a "
                    + "atividade tem progresso, tentativa ou ponto registrado. Exige conta NATY e "
                    + "vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Atividade removida"),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Atividade nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "A atividade tem historico. Codigo CONTEUDO_COM_PROGRESSO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void remover(
            @Parameter(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
                    @PathVariable
                    UUID atividadeId,
            IntegranteDaRequisicao quem) {
        atividadeService.remover(atividadeId, quem.usuarioId());
    }
}
