package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AtividadeAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.AtividadeCriacaoRequest;
import com.projetointegrador.natysync.trilha.dto.AtividadeDetalheResponse;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Conteudo", description = "Escrita do conteudo de treinamento, restrita a conta NATY")
public class AtividadeConteudoController {

    private final AtividadeConteudoService atividadeService;

    public AtividadeConteudoController(AtividadeConteudoService atividadeService) {
        this.atividadeService = atividadeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria uma atividade no fim do modulo",
            description = "Video deve ser URL HTTPS do YouTube com duracao positiva em segundos, e a duracao so existe"
                    + " com video. O XP vale 10 quando omitido.")
    public AtividadeDetalheResponse criar(
            @Valid @RequestBody AtividadeCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return atividadeService.criar(requisicao, quem.usuarioId());
    }

    @PutMapping("/{atividadeId}")
    @Operation(summary = "Altera os dados da atividade")
    public AtividadeDetalheResponse alterar(
            @PathVariable UUID atividadeId,
            @Valid @RequestBody AtividadeAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return atividadeService.alterar(atividadeId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{atividadeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove a atividade sem historico",
            description = "Responde 409 CONTEUDO_COM_PROGRESSO quando a atividade tem progresso, tentativa ou ponto.")
    public void remover(@PathVariable UUID atividadeId, IntegranteDaRequisicao quem) {
        atividadeService.remover(atividadeId, quem.usuarioId());
    }
}
