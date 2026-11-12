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
@Tag(name = "Conteúdo", description = "Escrita do conteúdo global de treinamento. Exige conta NATY.")
public class TrilhaConteudoController {

    private final TrilhaConteudoService trilhaService;
    private final ReordenacaoService reordenacaoService;

    public TrilhaConteudoController(TrilhaConteudoService trilhaService, ReordenacaoService reordenacaoService) {
        this.trilhaService = trilhaService;
        this.reordenacaoService = reordenacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar uma trilha", description = "Cria a trilha ativa no fim da ordem. Exige conta NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Trilha criada"),
        @ApiResponse(
                responseCode = "400",
                description = "Título ausente, em branco ou acima de 255",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public TrilhaAdminResponse criar(@Valid @RequestBody TrilhaRequest requisicao, IntegranteDaRequisicao quem) {
        return trilhaService.criar(requisicao, quem.usuarioId());
    }

    @PutMapping("/{trilhaId}")
    @Operation(
            summary = "Alterar uma trilha",
            description = "Substitui título e descrição. Ordem e ativação não mudam.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trilha alterada"),
        @ApiResponse(
                responseCode = "400",
                description = "Título ausente, em branco ou acima de 255",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Trilha não encontrada",
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
            summary = "Ativar ou desativar uma trilha",
            description =
                    "Trilha inativa sai da listagem e mantém o histórico. Use no lugar de remover trilha com histórico.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ativação gravada"),
        @ApiResponse(
                responseCode = "400",
                description = "Campo ativa ausente",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Trilha não encontrada",
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
            summary = "Remover uma trilha",
            description =
                    "Remove a trilha com tudo que ela contém. Recusada se houver progresso, tentativa ou ponto registrado.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Trilha removida"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Trilha não encontrada",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Trilha com progresso registrado",
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
            summary = "Reordenar os módulos",
            description = "Recebe todos os módulos da trilha na ordem desejada, sem repetição. Exige conta NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Módulos reordenados"),
        @ApiResponse(
                responseCode = "400",
                description = "Lista de identificadores ausente ou vazia",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Trilha não encontrada",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Ordem incompleta, repetida ou com item alheio",
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
