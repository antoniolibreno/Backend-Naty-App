package com.projetointegrador.natysync.trilha;

import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.trilha.dto.AtividadeDetalheResponse;
import com.projetointegrador.natysync.trilha.dto.QuizResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/atividades")
@Tag(name = "Atividades", description = "Atividade e quiz dela, sem a alternativa correta")
public class AtividadeController {

    private final TrilhaService trilhaService;

    public AtividadeController(TrilhaService trilhaService) {
        this.trilhaService = trilhaService;
    }

    @GetMapping("/{atividadeId}")
    @Operation(
            summary = "Consultar uma atividade",
            description = "Devolve imagem, vídeo, duração, pontos e se há quiz. Sem vídeo, videoUrl é nulo.")
    @ApiResponse(responseCode = "200", description = "Atividade encontrada")
    @ApiResponse(
            responseCode = "404",
            description = "Atividade não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public AtividadeDetalheResponse detalhar(
            @Parameter(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
                    @PathVariable
                    UUID atividadeId) {
        return trilhaService.buscarAtividade(atividadeId);
    }

    @GetMapping("/{atividadeId}/quiz")
    @Operation(
            summary = "Consultar o quiz da atividade",
            description = "Devolve a nota mínima, as perguntas e as alternativas, sem gabarito.")
    @ApiResponse(responseCode = "200", description = "Quiz sem gabarito")
    @ApiResponse(
            responseCode = "404",
            description = "Atividade ou quiz não encontrado",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public QuizResponse buscarQuiz(
            @Parameter(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
                    @PathVariable
                    UUID atividadeId) {
        return trilhaService.buscarQuizDaAtividade(atividadeId);
    }
}
