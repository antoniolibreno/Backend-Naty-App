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
@Tag(name = "Conteúdo", description = "Escrita do conteúdo global de treinamento. Exige conta NATY.")
public class AtividadeConteudoController {

    private final AtividadeConteudoService atividadeService;

    public AtividadeConteudoController(AtividadeConteudoService atividadeService) {
        this.atividadeService = atividadeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar uma atividade",
            description =
                    "Cria a atividade no fim do módulo. Vídeo, se houver, é HTTPS do YouTube com duração positiva. XP padrão 10. Exige conta NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Atividade criada"),
        @ApiResponse(
                responseCode = "400",
                description = "Módulo, título, vídeo ou duração inválidos",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Módulo não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public AtividadeDetalheResponse criar(
            @Valid @RequestBody AtividadeCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return atividadeService.criar(requisicao, quem.usuarioId());
    }

    @PutMapping("/{atividadeId}")
    @Operation(
            summary = "Alterar uma atividade",
            description =
                    "Substitui os dados da atividade. Vídeo, se houver, é HTTPS do YouTube com duração positiva. O XP já concedido não muda.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Atividade alterada"),
        @ApiResponse(
                responseCode = "400",
                description = "Título, vídeo ou duração inválidos",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Atividade não encontrada",
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
            summary = "Remover uma atividade",
            description =
                    "Remove a atividade com seu quiz. Recusada se houver progresso, tentativa ou ponto registrado.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Atividade removida"),
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
                description = "Atividade com progresso registrado",
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
