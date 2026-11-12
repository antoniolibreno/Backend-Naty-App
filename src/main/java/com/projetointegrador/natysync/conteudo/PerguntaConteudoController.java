package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.PerguntaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.PerguntaAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.PerguntaCriacaoRequest;
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
@RequestMapping("/api/v1/perguntas")
@Tag(
        name = "Conteudo",
        description =
                "Escrita do conteudo global de treinamento: trilha, modulo, atividade, quiz, pergunta e alternativa."
                        + " Toda rota de escrita exige conta NATY. A ordem de criacao acrescenta ao fim, e conteudo com historico nao e removido."
                        + " Cada alteracao grava um registro de auditoria.")
public class PerguntaConteudoController {

    private final QuizConteudoService quizService;

    public PerguntaConteudoController(QuizConteudoService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria uma pergunta no fim do quiz",
            description = "Cria a pergunta com suas alternativas e ordem igual ao maior valor atual do "
                    + "quiz mais 1. Exige ao menos duas alternativas e exatamente uma correta. Exige "
                    + "conta NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pergunta criada"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido, menos de duas alternativas ou sem exatamente uma correta. "
                        + "Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Quiz nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PerguntaAdminResponse criar(
            @Valid @RequestBody PerguntaCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return quizService.criarPergunta(requisicao, quem.usuarioId());
    }

    @PutMapping("/{perguntaId}")
    @Operation(
            summary = "Altera o enunciado da pergunta",
            description = "As alternativas mudam pelas rotas de alternativa. Exige conta NATY e vale para "
                    + "todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pergunta alterada"),
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
                description = "Pergunta nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PerguntaAdminResponse alterar(
            @Parameter(description = "Identificador da pergunta", example = "9f4a6c12-3e87-4d05-a2b9-5c1d7e8f0b36")
                    @PathVariable
                    UUID perguntaId,
            @Valid @RequestBody PerguntaAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return quizService.alterarPergunta(perguntaId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{perguntaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove a pergunta com suas alternativas",
            description = "Recusa a ultima pergunta do quiz, porque o quiz sem pergunta quebra a nota "
                    + "percentual, e recusa pergunta que alguma tentativa respondeu. Exige conta NATY "
                    + "e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Pergunta removida"),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Pergunta nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "E a ultima pergunta do quiz. Codigo QUIZ_SEM_PERGUNTA",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Alguma tentativa respondeu a pergunta. Codigo CONTEUDO_COM_PROGRESSO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void remover(
            @Parameter(description = "Identificador da pergunta", example = "9f4a6c12-3e87-4d05-a2b9-5c1d7e8f0b36")
                    @PathVariable
                    UUID perguntaId,
            IntegranteDaRequisicao quem) {
        quizService.removerPergunta(perguntaId, quem.usuarioId());
    }
}
