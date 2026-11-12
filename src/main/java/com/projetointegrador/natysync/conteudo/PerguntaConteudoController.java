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
@Tag(name = "Conteúdo", description = "Escrita do conteúdo global de treinamento. Exige conta NATY.")
public class PerguntaConteudoController {

    private final QuizConteudoService quizService;

    public PerguntaConteudoController(QuizConteudoService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar uma pergunta",
            description =
                    "Cria a pergunta no fim do quiz, com ao menos duas alternativas e exatamente uma correta. Exige conta NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pergunta criada"),
        @ApiResponse(
                responseCode = "400",
                description = "Quiz, enunciado ou alternativas inválidos",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Quiz não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PerguntaAdminResponse criar(
            @Valid @RequestBody PerguntaCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return quizService.criarPergunta(requisicao, quem.usuarioId());
    }

    @PutMapping("/{perguntaId}")
    @Operation(
            summary = "Alterar uma pergunta",
            description = "Substitui o enunciado. As alternativas mudam pelas rotas de alternativa.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pergunta alterada"),
        @ApiResponse(
                responseCode = "400",
                description = "Enunciado ausente ou em branco",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Pergunta não encontrada",
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
            summary = "Remover uma pergunta",
            description =
                    "Remove a pergunta com suas alternativas. Recusada para a última do quiz ou se já foi respondida.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Pergunta removida"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Pergunta não encontrada",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Última do quiz ou já respondida",
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
