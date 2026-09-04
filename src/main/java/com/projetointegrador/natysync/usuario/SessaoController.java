package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.usuario.dto.SessaoRequest;
import com.projetointegrador.natysync.usuario.dto.SessaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessoes")
@Tag(name = "Sessoes", description = "Resolucao provisoria de identidade do integrante")
public class SessaoController {

    private final UsuarioService usuarioService;

    public SessaoController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @Operation(
            summary = "Resolve o integrante a partir do e-mail",
            description = "PROVISORIO E SEM AUTENTICACAO. Nao emite token, credencial nem cookie de sessao."
                    + " Qualquer e-mail existente na base e aceito sem verificar a identidade de quem chama."
                    + " Substituido por autenticacao real em etapa futura.")
    public SessaoResponse resolver(@Valid @RequestBody SessaoRequest requisicao) {
        return usuarioService.resolverPorEmail(requisicao.email());
    }
}
