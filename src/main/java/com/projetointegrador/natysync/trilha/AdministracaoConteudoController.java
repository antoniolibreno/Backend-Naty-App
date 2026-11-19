package com.projetointegrador.natysync.trilha;

import com.projetointegrador.natysync.trilha.dto.AlternativaEscritaRequest;
import com.projetointegrador.natysync.trilha.dto.AtividadeEscritaRequest;
import com.projetointegrador.natysync.trilha.dto.ModuloEscritaRequest;
import com.projetointegrador.natysync.trilha.dto.OrdemEscritaRequest;
import com.projetointegrador.natysync.trilha.dto.PerguntaAtualizacaoRequest;
import com.projetointegrador.natysync.trilha.dto.PerguntaEscritaRequest;
import com.projetointegrador.natysync.trilha.dto.QuizEscritaRequest;
import com.projetointegrador.natysync.trilha.dto.TrilhaEscritaRequest;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Administração de conteúdo")
public class AdministracaoConteudoController {

    private final AdministracaoConteudoService service;

    public AdministracaoConteudoController(AdministracaoConteudoService service) {
        this.service = service;
    }

    @PostMapping("/trilhas")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> criarTrilha(@Valid @RequestBody TrilhaEscritaRequest request, Authentication auth) {
        return Map.of("id", service.criarTrilha(request, usuarioId(auth)));
    }

    @PutMapping("/trilhas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarTrilha(
            @PathVariable UUID id, @Valid @RequestBody TrilhaEscritaRequest request, Authentication auth) {
        service.atualizarTrilha(id, request, usuarioId(auth));
    }

    @DeleteMapping("/trilhas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerTrilha(@PathVariable UUID id, Authentication auth) {
        service.removerTrilha(id, usuarioId(auth));
    }

    @PutMapping("/trilhas/{id}/ativa")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativarTrilha(@PathVariable UUID id, @RequestBody Map<String, Boolean> body, Authentication auth) {
        service.ativarTrilha(id, Boolean.TRUE.equals(body.get("ativa")), usuarioId(auth));
    }

    @PostMapping("/modulos")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> criarModulo(@Valid @RequestBody ModuloEscritaRequest request, Authentication auth) {
        return Map.of("id", service.criarModulo(request, usuarioId(auth)));
    }

    @PutMapping("/modulos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarModulo(
            @PathVariable UUID id, @Valid @RequestBody ModuloEscritaRequest request, Authentication auth) {
        service.atualizarModulo(id, request, usuarioId(auth));
    }

    @DeleteMapping("/modulos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerModulo(@PathVariable UUID id, Authentication auth) {
        service.removerModulo(id, usuarioId(auth));
    }

    @PutMapping("/modulos/ordem")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reordenarModulos(@Valid @RequestBody OrdemEscritaRequest request, Authentication auth) {
        service.reordenarModulos(request.paiId(), request.ids(), usuarioId(auth));
    }

    @PostMapping("/atividades")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> criarAtividade(@Valid @RequestBody AtividadeEscritaRequest request, Authentication auth) {
        return Map.of("id", service.criarAtividade(request, usuarioId(auth)));
    }

    @PutMapping("/atividades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarAtividade(
            @PathVariable UUID id, @Valid @RequestBody AtividadeEscritaRequest request, Authentication auth) {
        service.atualizarAtividade(id, request, usuarioId(auth));
    }

    @DeleteMapping("/atividades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerAtividade(@PathVariable UUID id, Authentication auth) {
        service.removerAtividade(id, usuarioId(auth));
    }

    @PutMapping("/atividades/ordem")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reordenarAtividades(@Valid @RequestBody OrdemEscritaRequest request, Authentication auth) {
        service.reordenarAtividades(request.paiId(), request.ids(), usuarioId(auth));
    }

    @PostMapping("/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> criarQuiz(@Valid @RequestBody QuizEscritaRequest request, Authentication auth) {
        return Map.of("id", service.criarQuiz(request, usuarioId(auth)));
    }

    @PutMapping("/quizzes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarQuiz(
            @PathVariable UUID id, @Valid @RequestBody QuizEscritaRequest request, Authentication auth) {
        service.atualizarQuiz(id, request, usuarioId(auth));
    }

    @DeleteMapping("/quizzes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerQuiz(@PathVariable UUID id, Authentication auth) {
        service.removerQuiz(id, usuarioId(auth));
    }

    @PostMapping("/perguntas")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> criarPergunta(@Valid @RequestBody PerguntaEscritaRequest request, Authentication auth) {
        return Map.of("id", service.criarPergunta(request, usuarioId(auth)));
    }

    @PutMapping("/perguntas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarPergunta(
            @PathVariable UUID id, @Valid @RequestBody PerguntaAtualizacaoRequest request, Authentication auth) {
        service.atualizarPergunta(id, request, usuarioId(auth));
    }

    @DeleteMapping("/perguntas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerPergunta(@PathVariable UUID id, Authentication auth) {
        service.removerPergunta(id, usuarioId(auth));
    }

    @PostMapping("/perguntas/{perguntaId}/alternativas")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> criarAlternativa(
            @PathVariable UUID perguntaId, @Valid @RequestBody AlternativaEscritaRequest request, Authentication auth) {
        return Map.of(
                "id",
                service.criarAlternativa(
                        perguntaId, request.texto(), request.correta(), request.ordem(), usuarioId(auth)));
    }

    @PutMapping("/alternativas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarAlternativa(
            @PathVariable UUID id, @Valid @RequestBody AlternativaEscritaRequest request, Authentication auth) {
        service.atualizarAlternativa(id, request.texto(), request.correta(), request.ordem(), usuarioId(auth));
    }

    @DeleteMapping("/alternativas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerAlternativa(@PathVariable UUID id, Authentication auth) {
        service.removerAlternativa(id, usuarioId(auth));
    }

    private static UUID usuarioId(Authentication authentication) {
        return ((IntegranteDaRequisicao) authentication.getPrincipal()).usuarioId();
    }
}
