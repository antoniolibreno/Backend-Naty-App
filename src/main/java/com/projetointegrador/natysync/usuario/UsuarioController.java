package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Perfil do usuario autenticado")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @Operation(summary = "Busca o perfil do usuario autenticado")
    public UsuarioResponse meuPerfil(IntegranteDaRequisicao quem) {
        return usuarioService.buscarMeuPerfil(quem);
    }
}
