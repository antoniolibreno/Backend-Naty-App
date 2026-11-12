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
@Tag(
        name = "Progresso",
        description =
                "Progresso do integrante autenticado: desbloqueio linear, video, tentativas de quiz e conclusao. O integrante sempre vem da sessao, e o corpo nunca carrega nota nem estado")
public class ProgressoController {

    private final ProgressoService progressoService;

    public ProgressoController(ProgressoService progressoService) {
        this.progressoService = progressoService;
    }

    @GetMapping("/trilhas/{trilhaId}/progresso")
    @Operation(
            summary = "Devolve a trilha com o estado de cada atividade para o integrante",
            description =
                    "Estado por atividade: BLOQUEADO, DISPONIVEL ou CONCLUIDO. O desbloqueio e linear: so a primeira"
                            + " atividade nao concluida fica DISPONIVEL, e as seguintes ficam BLOQUEADO. Traz totais, percentual"
                            + " e a proxima atividade, calculados para o integrante da sessao.")
    @ApiResponse(responseCode = "200", description = "Trilha com o estado de cada atividade")
    @ApiResponse(
            responseCode = "404",
            description = "Trilha nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
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
            summary = "Resume o progresso do integrante somando as trilhas ativas",
            description = "Devolve total de atividades, concluidas, percentual e a proxima atividade, para a propria"
                    + " conta ou colega ativo da mesma empresa. Outra empresa, conta desativada e identificador"
                    + " inexistente respondem 404.")
    @ApiResponse(responseCode = "200", description = "Resumo do progresso do integrante")
    @ApiResponse(
            responseCode = "404",
            description =
                    "Integrante inexistente, desativado ou de outra empresa, todos com a mesma resposta. Codigo RECURSO_NAO_ENCONTRADO",
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
            summary = "Registra a assistencia do video; so conclui atividade sem quiz",
            description =
                    "Atividade com video configurado exige inicio registrado em video-iniciado e tempo decorrido"
                            + " igual ou superior a duracaoSegundos. O servidor confere o tempo, nao a reproducao no player."
                            + " Atividade sem quiz conclui neste registro. Atividade com quiz conclui so por tentativa aprovada."
                            + " Repetir a chamada preserva a assistencia e a conclusao ja gravadas. Exige a atividade desbloqueada.")
    @ApiResponse(
            responseCode = "200",
            description = "Assistencia registrada, com o estado da atividade e a proxima atividade")
    @ApiResponse(
            responseCode = "404",
            description = "Atividade nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "409",
            description =
                    "Conflito de video. Codigos VIDEO_NAO_INICIADO (inicio nao registrado),"
                            + " VIDEO_TEMPO_INSUFICIENTE (tempo decorrido menor que a duracao) e"
                            + " VIDEO_NAO_CONFIGURADO (video sem duracao positiva). Atividade bloqueada responde ATIVIDADE_BLOQUEADA",
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
            summary = "Registra o primeiro inicio do video para conferir a duracao da assistencia",
            description =
                    "Idempotente por integrante e atividade: repetir a chamada preserva o instante do primeiro"
                            + " inicio. Esse instante mais duracaoSegundos define o tempo minimo para registrar a assistencia"
                            + " em video-assistido. Exige a atividade desbloqueada e com video e duracao positiva configurados.")
    @ApiResponse(responseCode = "200", description = "Inicio registrado, ou o inicio anterior repetido")
    @ApiResponse(
            responseCode = "404",
            description = "Atividade nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Atividade bloqueada, codigo ATIVIDADE_BLOQUEADA, ou atividade sem video com duracao"
                    + " configurada, codigo VIDEO_NAO_CONFIGURADO",
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
            summary = "Corrige e registra uma tentativa do quiz",
            description =
                    "Exige uma resposta para cada pergunta do quiz, exatamente uma vez, com alternativa da propria"
                            + " pergunta. A nota e a porcentagem de respostas corretas arredondada para inteiro, calculada pelo"
                            + " servidor. A tentativa e aprovada quando a nota e igual ou superior a notaMinima do quiz, e a"
                            + " aprovacao conclui a atividade. Reprovar nao altera conclusao anterior nem apaga historico."
                            + " A resposta traz as perguntas erradas, nunca a alternativa correta. Tentativa invalida nao grava"
                            + " historico. Exige a atividade desbloqueada.")
    @ApiResponse(responseCode = "200", description = "Tentativa corrigida e registrada, aprovada ou nao")
    @ApiResponse(
            responseCode = "400",
            description = "Corpo invalido. Codigo FALHA_DE_VALIDACAO para campo ausente ou lista vazia, e codigo"
                    + " TENTATIVA_QUIZ_INVALIDA para pergunta repetida, pergunta de outro quiz, pergunta sem resposta"
                    + " ou alternativa que nao pertence a pergunta",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Atividade nao encontrada ou atividade sem quiz. Codigo RECURSO_NAO_ENCONTRADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "409",
            description =
                    "Atividade bloqueada: a atividade anterior da trilha nao foi concluida. Codigo ATIVIDADE_BLOQUEADA",
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
