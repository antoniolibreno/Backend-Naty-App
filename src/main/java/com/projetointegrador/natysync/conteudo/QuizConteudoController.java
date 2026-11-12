package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.QuizAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.QuizAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.QuizCriacaoRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quizzes")
@Tag(
        name = "Conteudo",
        description =
                "Escrita do conteudo global de treinamento: trilha, modulo, atividade, quiz, pergunta e alternativa."
                        + " Toda rota de escrita exige conta NATY. A ordem de criacao acrescenta ao fim, e conteudo com historico nao e removido."
                        + " Cada alteracao grava um registro de auditoria.")
public class QuizConteudoController {

    private final QuizConteudoService quizService;

    public QuizConteudoController(QuizConteudoService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{quizId}")
    @Operation(
            summary = "Le o quiz com o gabarito",
            description = "Unico canal que devolve o campo correta de cada alternativa. A leitura do "
                    + "integrante em /api/v1/atividades/{id}/quiz nao tem o campo. Exige conta NATY e "
                    + "vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Quiz com o gabarito"),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Quiz nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public QuizAdminResponse buscar(
            @Parameter(description = "Identificador do quiz", example = "3b8e2d71-9c40-4a65-b1f8-7d5e0a2c4f93")
                    @PathVariable
                    UUID quizId) {
        return quizService.buscar(quizId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria o quiz de uma atividade com suas perguntas e alternativas",
            description = "Nota minima de 0 a 100, padrao 70. Cada pergunta tem ao menos duas "
                    + "alternativas e exatamente uma correta. Cada atividade tem no maximo um quiz. "
                    + "Com quiz, a atividade conclui por tentativa aprovada para quem nao a "
                    + "concluiu. Exige conta NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Quiz criado"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido, pergunta com menos de duas alternativas ou sem exatamente uma "
                        + "correta. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
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
                description = "A atividade ja tem quiz. Codigo QUIZ_JA_EXISTE",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public QuizAdminResponse criar(@Valid @RequestBody QuizCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return quizService.criarQuiz(requisicao, quem.usuarioId());
    }

    @PutMapping("/{quizId}")
    @Operation(
            summary = "Altera a nota minima do quiz",
            description = "A tentativa antiga mantem a nota gravada, e a nova nota minima vale para as "
                    + "tentativas seguintes. Exige conta NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Quiz alterado"),
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
                description = "Quiz nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public QuizAdminResponse alterar(
            @Parameter(description = "Identificador do quiz", example = "3b8e2d71-9c40-4a65-b1f8-7d5e0a2c4f93")
                    @PathVariable
                    UUID quizId,
            @Valid @RequestBody QuizAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return quizService.alterarQuiz(quizId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{quizId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove o quiz com suas perguntas e alternativas",
            description = "Recusa quando a atividade tem progresso, tentativa ou ponto registrado, porque "
                    + "a regra de conclusao dela mudaria. Exige conta NATY e vale para todas as "
                    + "empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Quiz removido"),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Quiz nao encontrado. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "A atividade do quiz tem historico. Codigo CONTEUDO_COM_PROGRESSO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void remover(
            @Parameter(description = "Identificador do quiz", example = "3b8e2d71-9c40-4a65-b1f8-7d5e0a2c4f93")
                    @PathVariable
                    UUID quizId,
            IntegranteDaRequisicao quem) {
        quizService.removerQuiz(quizId, quem.usuarioId());
    }
}
