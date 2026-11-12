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
@Tag(name = "Conteúdo", description = "Escrita do conteúdo global de treinamento. Exige conta NATY.")
public class ModuloConteudoController {

    private final ModuloConteudoService moduloService;
    private final ReordenacaoService reordenacaoService;

    public ModuloConteudoController(ModuloConteudoService moduloService, ReordenacaoService reordenacaoService) {
        this.moduloService = moduloService;
        this.reordenacaoService = reordenacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar um módulo", description = "Cria o módulo no fim da trilha. Exige conta NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Módulo criado"),
        @ApiResponse(
                responseCode = "400",
                description = "Trilha ausente, ou título inválido",
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
    public ModuloResponse criar(@Valid @RequestBody ModuloCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return moduloService.criar(requisicao, quem.usuarioId());
    }

    @PutMapping("/{moduloId}")
    @Operation(summary = "Alterar um módulo", description = "Substitui título e descrição. A ordem não muda.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Módulo alterado"),
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
                description = "Módulo não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ModuloResponse alterar(
            @Parameter(description = "Identificador do módulo", example = "5d1f8a63-7e2c-4b90-a4d5-1f3b9c6e8a07")
                    @PathVariable
                    UUID moduloId,
            @Valid @RequestBody ModuloAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return moduloService.alterar(moduloId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{moduloId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remover um módulo",
            description =
                    "Remove o módulo com tudo que ele contém. Recusada se houver progresso, tentativa ou ponto registrado.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Módulo removido"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Módulo não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Módulo com progresso registrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void remover(
            @Parameter(description = "Identificador do módulo", example = "5d1f8a63-7e2c-4b90-a4d5-1f3b9c6e8a07")
                    @PathVariable
                    UUID moduloId,
            IntegranteDaRequisicao quem) {
        moduloService.remover(moduloId, quem.usuarioId());
    }

    @PutMapping("/{moduloId}/atividades/ordem")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Reordenar as atividades",
            description = "Recebe todas as atividades do módulo na ordem desejada, sem repetição. Exige conta NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Atividades reordenadas"),
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
                description = "Módulo não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Ordem incompleta, repetida ou com item alheio",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public void reordenarAtividades(
            @Parameter(description = "Identificador do módulo", example = "5d1f8a63-7e2c-4b90-a4d5-1f3b9c6e8a07")
                    @PathVariable
                    UUID moduloId,
            @Valid @RequestBody OrdemRequest requisicao,
            IntegranteDaRequisicao quem) {
        reordenacaoService.reordenarAtividades(moduloId, requisicao.ids(), quem.usuarioId());
    }
}
