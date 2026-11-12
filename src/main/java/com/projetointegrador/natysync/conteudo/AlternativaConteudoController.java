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
@Tag(
        name = "Conteudo",
        description =
                "Escrita do conteudo global de treinamento: trilha, modulo, atividade, quiz, pergunta e alternativa."
                        + " Toda rota de escrita exige conta NATY. A ordem de criacao acrescenta ao fim, e conteudo com historico nao e removido."
                        + " Cada alteracao grava um registro de auditoria.")
public class AlternativaConteudoController {

    private final QuizConteudoService quizService;

    public AlternativaConteudoController(QuizConteudoService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria uma alternativa no fim da pergunta",
            description = "Com correta verdadeiro, a marca de correta se move da alternativa anterior "
                    + "para a nova na mesma transacao e grava dois registros ALTERACAO. Exige conta "
                    + "NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Alternativa criada"),
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
    public AlternativaAdminResponse criar(
            @Valid @RequestBody AlternativaCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return quizService.criarAlternativa(requisicao, quem.usuarioId());
    }

    @PutMapping("/{alternativaId}")
    @Operation(
            summary = "Altera o texto e a marca de correta da alternativa",
            description = "Com correta verdadeiro, a marca se move da alternativa anterior para esta. "
                    + "Desmarcar a unica correta e recusado, porque toda pergunta tem exatamente uma "
                    + "correta. Exige conta NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Alternativa alterada"),
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
                description = "Alternativa nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Desmarcar a unica alternativa correta deixaria a pergunta sem gabarito. Codigo "
                        + "GABARITO_INVALIDO",
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
            summary = "Remove a alternativa",
            description = "Recusa a alternativa correta e a remocao que deixaria a pergunta com menos de "
                    + "duas alternativas, e recusa alternativa que alguma tentativa escolheu. Exige "
                    + "conta NATY e vale para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Alternativa removida"),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Alternativa nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "A alternativa e a correta ou restariam menos de duas. Codigo GABARITO_INVALIDO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Alguma tentativa escolheu a alternativa. Codigo CONTEUDO_COM_PROGRESSO",
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
