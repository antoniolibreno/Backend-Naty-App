package com.projetointegrador.natysync.trilha;

import com.projetointegrador.natysync.trilha.dto.TrilhaDetalheResponse;
import com.projetointegrador.natysync.trilha.dto.TrilhaResumoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trilhas")
@Tag(name = "Trilhas", description = "Conteudo do treinamento")
public class TrilhaController {

    private final TrilhaService trilhaService;

    public TrilhaController(TrilhaService trilhaService) {
        this.trilhaService = trilhaService;
    }

    @GetMapping
    @Operation(summary = "Lista as trilhas ativas em ordem crescente")
    public List<TrilhaResumoResponse> listar() {
        return trilhaService.listarAtivas();
    }

    @GetMapping("/{trilhaId}")
    @Operation(summary = "Detalha uma trilha com seus modulos e atividades")
    public TrilhaDetalheResponse detalhar(@PathVariable UUID trilhaId) {
        return trilhaService.buscarDetalhe(trilhaId);
    }
}
