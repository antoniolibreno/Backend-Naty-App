package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AlternativaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.AlternativaCriacaoRequest;
import com.projetointegrador.natysync.conteudo.dto.AlternativaRequest;
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
@RequestMapping("/api/v1/alternativas")
@Tag(name = "Conteúdo", description = "Escrita do conteúdo global de treinamento. Exige conta NATY.")
public class AlternativaConteudoController {

    private final QuizConteudoService quizService;

    public AlternativaConteudoController(QuizConteudoService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar uma alternativa",
            description =
                    "Cria a alternativa no fim da pergunta. Marcada como correta, toma a marca da anterior. Exige conta NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Alternativa criada"),
        @ApiResponse(
                responseCode = "400",
                description = "Pergunta, texto ou campo correta inválidos",
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
    public AlternativaAdminResponse criar(
            @Valid @RequestBody AlternativaCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return quizService.criarAlternativa(requisicao, quem.usuarioId());
    }

    @PutMapping("/{alternativaId}")
    @Operation(
            summary = "Alterar uma alternativa",
            description = "Marcada como correta, toma a marca da anterior. Desmarcar a única correta é recusado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Alternativa alterada"),
        @ApiResponse(
                responseCode = "400",
                description = "Texto em branco ou campo correta ausente",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Alternativa não encontrada",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Pergunta ficaria sem alternativa correta",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public AlternativaAdminResponse alterar(
            @Parameter(description = "Identificador da alternativa", example = "6e0b3d95-8a21-4c7f-9d14-2f5a8c1e7b40")
                    @PathVariable
                    UUID alternativaId,
            @Valid @RequestBody AlternativaRequest requisicao,
            IntegranteDaRequisicao quem) {
        return quizService.alterarAlternativa(alternativaId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{alternativaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remover uma alternativa",
            description =
                    "Recusada para a alternativa correta, a que deixaria menos de duas ou a já escolhida em tentativa.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Alternativa removida"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Alternativa não encontrada",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Correta, mínimo de duas ou já escolhida",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void remover(
            @Parameter(description = "Identificador da alternativa", example = "6e0b3d95-8a21-4c7f-9d14-2f5a8c1e7b40")
                    @PathVariable
                    UUID alternativaId,
            IntegranteDaRequisicao quem) {
        quizService.removerAlternativa(alternativaId, quem.usuarioId());
    }
}
