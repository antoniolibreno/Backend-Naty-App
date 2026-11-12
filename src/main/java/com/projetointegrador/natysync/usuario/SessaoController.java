package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.usuario.dto.SessaoRequest;
import com.projetointegrador.natysync.usuario.dto.SessaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
@Tag(
        name = "Sessoes",
        description =
                "Abertura, renovacao e encerramento da sessao do integrante. A abertura e publica, e as demais rotas exigem o token da sessao em Authorization: Bearer")
public class SessaoController {

    private final SessaoService sessaoService;

    public SessaoController(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @PostMapping
    @SecurityRequirements
    @Operation(
            summary = "Autentica o integrante e emite a sessao",
            description = "Verifica a senha contra a credencial guardada e devolve um token opaco com a"
                    + " expiracao dele. E-mail nao cadastrado e senha incorreta devolvem a mesma recusa,"
                    + " para a resposta nao revelar quais e-mails existem. Integrante desativado e integrante de"
                    + " empresa inativa nao obtem sessao. O token viaja nas demais chamadas em Authorization: Bearer.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessao emitida com o token e a expiracao"),
        @ApiResponse(
                responseCode = "400",
                description = "Corpo invalido. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "401",
                description =
                        "E-mail inexistente, senha incorreta, conta inativa ou empresa inativa, todos com a mesma recusa. Codigo CREDENCIAL_INVALIDA",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public SessaoResponse autenticar(@Valid @RequestBody SessaoRequest requisicao) {
        return sessaoService.autenticar(requisicao);
    }

    @PostMapping("/atual/renovacao")
    @Operation(
            summary = "Renova a sessao em uso",
            description = "Com token valido, emite um token novo com o mesmo corpo da emissao e revoga o token"
                    + " apresentado. A expiracao nunca passa da duracao maxima contada do login. Token expirado,"
                    + " revogado ou de integrante que nao pode mais entrar devolve 401.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Sessao nova emitida e token apresentado revogado")
    })
    public SessaoResponse renovar(@RequestHeader(TokenSessaoFiltro.CABECALHO_AUTORIZACAO) String autorizacao) {
        return sessaoService.renovar(
                autorizacao.substring(TokenSessaoFiltro.PREFIXO_BEARER.length()).trim());
    }

    @DeleteMapping("/atual")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Revoga a sessao em uso",
            description = "O token apresentado deixa de ser aceito na chamada seguinte.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Sessao revogada, sem corpo")})
    public void revogar(@RequestHeader(TokenSessaoFiltro.CABECALHO_AUTORIZACAO) String autorizacao) {
        sessaoService.revogar(
                autorizacao.substring(TokenSessaoFiltro.PREFIXO_BEARER.length()).trim());
    }
}
