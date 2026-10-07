package com.projetointegrador.natysync.progresso;

import com.projetointegrador.natysync.progresso.dto.TentativaQuizRequest;
import com.projetointegrador.natysync.progresso.dto.TentativaQuizResponse;
import com.projetointegrador.natysync.progresso.dto.TrilhaProgressoResponse;
import com.projetointegrador.natysync.progresso.dto.VideoAssistidoResponse;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Progresso", description = "Estado do integrante na trilha")
public class ProgressoController {

    private final ProgressoService progressoService;

    public ProgressoController(ProgressoService progressoService) {
        this.progressoService = progressoService;
    }

    @GetMapping("/trilhas/{trilhaId}/progresso")
    @Operation(summary = "Devolve a trilha com o estado de cada atividade para o integrante")
    public TrilhaProgressoResponse detalharProgresso(@PathVariable UUID trilhaId, IntegranteDaRequisicao integrante) {
        return progressoService.buscarTrilhaComProgresso(trilhaId, integrante);
    }

    @PostMapping("/atividades/{atividadeId}/video-assistido")
    @Operation(summary = "Registra a assistência do vídeo; só conclui atividade sem quiz")
    public VideoAssistidoResponse registrarVideoAssistido(
            @PathVariable UUID atividadeId, IntegranteDaRequisicao integrante) {
        return progressoService.registrarVideoAssistido(atividadeId, integrante);
    }

    @PostMapping("/atividades/{atividadeId}/quiz/tentativas")
    @Operation(summary = "Corrige e registra uma tentativa do quiz")
    public TentativaQuizResponse registrarTentativa(
            @PathVariable UUID atividadeId,
            @Valid @RequestBody TentativaQuizRequest requisicao,
            IntegranteDaRequisicao integrante) {
        return progressoService.registrarTentativa(atividadeId, requisicao, integrante);
    }
}
