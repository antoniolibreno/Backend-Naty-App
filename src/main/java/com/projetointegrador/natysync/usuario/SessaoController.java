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
@Tag(name = "Sessões", description = "Abertura, renovação e encerramento da sessão. Só a abertura é pública.")
public class SessaoController {

    private final SessaoService sessaoService;

    public SessaoController(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @PostMapping
    @SecurityRequirements
    @Operation(
            summary = "Abrir uma sessão",
            description =
                    "Devolve um token opaco e a expiração, para uso em Authorization: Bearer. E-mail desconhecido e senha incorreta recebem a mesma recusa. Conta ou empresa inativa não obtém sessão.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessão emitida"),
        @ApiResponse(
                responseCode = "400",
                description = "E-mail ou senha ausente ou inválido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Credencial inválida ou conta inativa",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public SessaoResponse autenticar(@Valid @RequestBody SessaoRequest requisicao) {
        return sessaoService.autenticar(requisicao);
    }

    @PostMapping("/atual/renovacao")
    @Operation(
            summary = "Renovar a sessão",
            description =
                    "Emite um token novo e revoga o apresentado. A expiração respeita a duração máxima contada do login.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Sessão renovada")})
    public SessaoResponse renovar(@RequestHeader(TokenSessaoFiltro.CABECALHO_AUTORIZACAO) String autorizacao) {
        return sessaoService.renovar(
                autorizacao.substring(TokenSessaoFiltro.PREFIXO_BEARER.length()).trim());
    }

    @DeleteMapping("/atual")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerrar a sessão", description = "O token apresentado deixa de ser aceito.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Sessão revogada")})
    public void revogar(@RequestHeader(TokenSessaoFiltro.CABECALHO_AUTORIZACAO) String autorizacao) {
        sessaoService.revogar(
                autorizacao.substring(TokenSessaoFiltro.PREFIXO_BEARER.length()).trim());
    }
}
