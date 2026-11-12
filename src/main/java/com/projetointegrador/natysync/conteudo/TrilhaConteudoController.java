package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AtivacaoRequest;
import com.projetointegrador.natysync.conteudo.dto.OrdemRequest;
import com.projetointegrador.natysync.conteudo.dto.TrilhaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.TrilhaRequest;
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
@RequestMapping("/api/v1/trilhas")
@Tag(name = "Conteudo", description = "Escrita do conteudo de treinamento, restrita a conta NATY")
public class TrilhaConteudoController {

    private final TrilhaConteudoService trilhaService;
    private final ReordenacaoService reordenacaoService;

    public TrilhaConteudoController(TrilhaConteudoService trilhaService, ReordenacaoService reordenacaoService) {
        this.trilhaService = trilhaService;
        this.reordenacaoService = reordenacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria uma trilha ativa no fim da ordem",
            description = "Exige conta NATY. O conteudo e global e vale para todas as empresas.")
    public TrilhaAdminResponse criar(@Valid @RequestBody TrilhaRequest requisicao, IntegranteDaRequisicao quem) {
        return trilhaService.criar(requisicao, quem.usuarioId());
    }

    @PutMapping("/{trilhaId}")
    @Operation(summary = "Altera titulo e descricao da trilha")
    public TrilhaAdminResponse alterar(
            @PathVariable UUID trilhaId, @Valid @RequestBody TrilhaRequest requisicao, IntegranteDaRequisicao quem) {
        return trilhaService.alterar(trilhaId, requisicao, quem.usuarioId());
    }

    @PutMapping("/{trilhaId}/ativa")
    @Operation(
            summary = "Ativa ou desativa a trilha",
            description =
                    "Trilha inativa sai da listagem e mantem o progresso, a tentativa e o ponto de quem a estudou.")
    public TrilhaAdminResponse definirAtivacao(
            @PathVariable UUID trilhaId, @Valid @RequestBody AtivacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return trilhaService.definirAtivacao(trilhaId, requisicao.ativa(), quem.usuarioId());
    }

    @DeleteMapping("/{trilhaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove a trilha sem historico",
            description = "Responde 409 CONTEUDO_COM_PROGRESSO quando alguma atividade tem progresso, tentativa ou"
                    + " ponto registrado. A alternativa e desativar a trilha.")
    public void remover(@PathVariable UUID trilhaId, IntegranteDaRequisicao quem) {
        trilhaService.remover(trilhaId, quem.usuarioId());
    }

    @PutMapping("/{trilhaId}/modulos/ordem")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Reordena os modulos da trilha",
            description = "Recebe todos os identificadores dos modulos na ordem desejada. Lista diferente dos modulos"
                    + " atuais responde 409 ORDEM_INVALIDA.")
    public void reordenarModulos(
            @PathVariable UUID trilhaId, @Valid @RequestBody OrdemRequest requisicao, IntegranteDaRequisicao quem) {
        reordenacaoService.reordenarModulos(trilhaId, requisicao.ids(), quem.usuarioId());
    }
}
