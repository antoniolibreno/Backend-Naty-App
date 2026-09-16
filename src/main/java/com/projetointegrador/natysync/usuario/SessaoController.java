package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.usuario.dto.SessaoRequest;
import com.projetointegrador.natysync.usuario.dto.SessaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessoes")
@Tag(name = "Sessoes", description = "Autenticacao do integrante e ciclo de vida da sessao")
public class SessaoController {

    private final SessaoService sessaoService;

    public SessaoController(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @PostMapping
    @Operation(
            summary = "Autentica o integrante e emite a sessao",
            description = "Verifica a senha contra a credencial guardada e devolve um token opaco com a"
                    + " expiracao dele. E-mail nao cadastrado e senha incorreta devolvem a mesma recusa,"
                    + " para a resposta nao revelar quais e-mails existem. Integrante desativado nao obtem"
                    + " sessao. O token viaja nas demais chamadas em Authorization: Bearer.")
    public SessaoResponse autenticar(@Valid @RequestBody SessaoRequest requisicao) {
        return sessaoService.autenticar(requisicao);
    }

    @DeleteMapping("/atual")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Revoga a sessao em uso",
            description = "O token apresentado deixa de ser aceito na chamada seguinte.")
    public void revogar(@RequestHeader(TokenSessaoFiltro.CABECALHO_AUTORIZACAO) String autorizacao) {
        sessaoService.revogar(
                autorizacao.substring(TokenSessaoFiltro.PREFIXO_BEARER.length()).trim());
    }
}
