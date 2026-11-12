package com.projetointegrador.natysync.progresso;

import com.projetointegrador.natysync.progresso.dto.ResumoProgressoResponse;
import com.projetointegrador.natysync.progresso.dto.TentativaQuizRequest;
import com.projetointegrador.natysync.progresso.dto.TentativaQuizResponse;
import com.projetointegrador.natysync.progresso.dto.TrilhaProgressoResponse;
import com.projetointegrador.natysync.progresso.dto.VideoAssistidoResponse;
import com.projetointegrador.natysync.progresso.dto.VideoIniciadoResponse;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Progresso", description = "Progresso do integrante da sessão: desbloqueio, vídeo, quiz e conclusão")
public class ProgressoController {

    private final ProgressoService progressoService;

    public ProgressoController(ProgressoService progressoService) {
        this.progressoService = progressoService;
    }

    @GetMapping("/trilhas/{trilhaId}/progresso")
    @Operation(
            summary = "Consultar progresso na trilha",
            description = "Devolve o estado de cada atividade. O desbloqueio é linear: só a primeira não concluída"
                    + " fica DISPONIVEL e as seguintes ficam BLOQUEADO.")
    @ApiResponse(responseCode = "200", description = "Trilha com progresso")
    @ApiResponse(
            responseCode = "404",
            description = "Trilha não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public TrilhaProgressoResponse detalharProgresso(
            @Parameter(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
                    @PathVariable
                    UUID trilhaId,
            IntegranteDaRequisicao integrante) {
        return progressoService.buscarTrilhaComProgresso(trilhaId, integrante);
    }

    @GetMapping("/integrantes/{integranteId}/progresso")
    @Operation(
            summary = "Resumir progresso do integrante",
            description = "Soma as trilhas ativas da própria conta ou de colega ativo da mesma empresa.")
    @ApiResponse(responseCode = "200", description = "Resumo do progresso")
    @ApiResponse(
            responseCode = "404",
            description = "Integrante não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public ResumoProgressoResponse resumirProgresso(
            @Parameter(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
                    @PathVariable
                    UUID integranteId,
            IntegranteDaRequisicao quem) {
        return progressoService.resumir(integranteId, quem.empresaId());
    }

    @PostMapping("/atividades/{atividadeId}/video-assistido")
    @Operation(
            summary = "Registrar vídeo assistido",
            description = "Com vídeo, exige início registrado e a duração decorrida. Conclui só atividade sem quiz."
                    + " Exige atividade desbloqueada.")
    @ApiResponse(responseCode = "200", description = "Assistência registrada")
    @ApiResponse(
            responseCode = "404",
            description = "Atividade não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Atividade bloqueada, vídeo não iniciado ou tempo incompleto",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public VideoAssistidoResponse registrarVideoAssistido(
            @Parameter(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
                    @PathVariable
                    UUID atividadeId,
            IntegranteDaRequisicao integrante) {
        return progressoService.registrarVideoAssistido(atividadeId, integrante);
    }

    @PostMapping("/atividades/{atividadeId}/video-iniciado")
    @Operation(
            summary = "Registrar início do vídeo",
            description = "Idempotente: repetir mantém o primeiro instante. Exige atividade desbloqueada, com vídeo"
                    + " e duração positiva.")
    @ApiResponse(responseCode = "200", description = "Início registrado")
    @ApiResponse(
            responseCode = "404",
            description = "Atividade não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Atividade bloqueada ou sem vídeo",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public VideoIniciadoResponse registrarVideoIniciado(
            @Parameter(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
                    @PathVariable
                    UUID atividadeId,
            IntegranteDaRequisicao integrante) {
        return progressoService.registrarVideoIniciado(atividadeId, integrante);
    }

    @PostMapping("/atividades/{atividadeId}/quiz/tentativas")
    @Operation(
            summary = "Registrar tentativa de quiz",
            description = "Exige uma resposta por pergunta. Nota igual ou superior à mínima aprova e conclui a"
                    + " atividade. Devolve as perguntas erradas, sem gabarito.")
    @ApiResponse(responseCode = "200", description = "Tentativa corrigida")
    @ApiResponse(
            responseCode = "400",
            description = "Respostas inválidas, repetidas ou incompletas",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Atividade ou quiz não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Atividade bloqueada",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public TentativaQuizResponse registrarTentativa(
            @Parameter(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
                    @PathVariable
                    UUID atividadeId,
            @Valid @RequestBody TentativaQuizRequest requisicao,
            IntegranteDaRequisicao integrante) {
        return progressoService.registrarTentativa(atividadeId, requisicao, integrante);
    }
}
