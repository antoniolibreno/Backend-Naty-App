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
@Tag(name = "Conteúdo", description = "Escrita do conteúdo global de treinamento. Exige conta NATY.")
public class QuizConteudoController {

    private final QuizConteudoService quizService;

    public QuizConteudoController(QuizConteudoService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{quizId}")
    @Operation(
            summary = "Ler um quiz com gabarito",
            description = "Único canal que devolve o campo correta das alternativas. Exige conta NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Quiz com gabarito"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Quiz não encontrado",
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
            summary = "Criar um quiz",
            description =
                    "Cria o quiz da atividade. Nota mínima de 0 a 100, padrão 70. Cada pergunta tem exatamente uma correta. Exige conta NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Quiz criado"),
        @ApiResponse(
                responseCode = "400",
                description = "Atividade, nota ou perguntas inválidas",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Atividade não encontrada",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Atividade com quiz",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public QuizAdminResponse criar(@Valid @RequestBody QuizCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return quizService.criarQuiz(requisicao, quem.usuarioId());
    }

    @PutMapping("/{quizId}")
    @Operation(
            summary = "Alterar a nota mínima",
            description = "A nova nota vale para as próximas tentativas. As antigas mantêm a nota gravada.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Quiz alterado"),
        @ApiResponse(
                responseCode = "400",
                description = "Nota ausente ou fora de 0 a 100",
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
            summary = "Remover um quiz",
            description =
                    "Remove o quiz com suas perguntas. Recusada se a atividade tiver progresso, tentativa ou ponto.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Quiz removido"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Quiz não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Atividade com progresso registrado",
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
