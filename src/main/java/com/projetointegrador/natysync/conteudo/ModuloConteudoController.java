package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.ModuloAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.ModuloCriacaoRequest;
import com.projetointegrador.natysync.conteudo.dto.OrdemRequest;
import com.projetointegrador.natysync.trilha.dto.ModuloResponse;
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
@RequestMapping("/api/v1/modulos")
@Tag(name = "Conteudo", description = "Escrita do conteudo de treinamento, restrita a conta NATY")
public class ModuloConteudoController {

    private final ModuloConteudoService moduloService;
    private final ReordenacaoService reordenacaoService;

    public ModuloConteudoController(ModuloConteudoService moduloService, ReordenacaoService reordenacaoService) {
        this.moduloService = moduloService;
        this.reordenacaoService = reordenacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um modulo no fim da trilha")
    public ModuloResponse criar(@Valid @RequestBody ModuloCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return moduloService.criar(requisicao, quem.usuarioId());
    }

    @PutMapping("/{moduloId}")
    @Operation(summary = "Altera titulo e descricao do modulo")
    public ModuloResponse alterar(
            @PathVariable UUID moduloId,
            @Valid @RequestBody ModuloAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return moduloService.alterar(moduloId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{moduloId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove o modulo sem historico",
            description = "Responde 409 CONTEUDO_COM_PROGRESSO quando alguma atividade dele tem progresso, tentativa"
                    + " ou ponto registrado.")
    public void remover(@PathVariable UUID moduloId, IntegranteDaRequisicao quem) {
        moduloService.remover(moduloId, quem.usuarioId());
    }

    @PutMapping("/{moduloId}/atividades/ordem")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Reordena as atividades do modulo",
            description = "Recebe todos os identificadores das atividades na ordem desejada. Lista diferente das"
                    + " atividades atuais responde 409 ORDEM_INVALIDA.")
    public void reordenarAtividades(
            @PathVariable UUID moduloId, @Valid @RequestBody OrdemRequest requisicao, IntegranteDaRequisicao quem) {
        reordenacaoService.reordenarAtividades(moduloId, requisicao.ids(), quem.usuarioId());
    }
}
