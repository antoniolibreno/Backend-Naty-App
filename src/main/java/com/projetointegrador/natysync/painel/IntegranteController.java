package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.painel.dto.AtivoRequest;
import com.projetointegrador.natysync.painel.dto.IntegranteAlteracaoRequest;
import com.projetointegrador.natysync.painel.dto.IntegranteCriacaoRequest;
import com.projetointegrador.natysync.painel.dto.IntegranteFiltro;
import com.projetointegrador.natysync.painel.dto.IntegranteResponse;
import com.projetointegrador.natysync.painel.dto.SenhaRequest;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/painel/integrantes")
@Tag(name = "Painel: integrantes", description = "Integrantes da empresa de quem chama. Exige o papel ADMIN.")
public class IntegranteController {

    private final IntegranteService integranteService;

    public IntegranteController(IntegranteService integranteService) {
        this.integranteService = integranteService;
    }

    @GetMapping
    @Operation(
            summary = "Lista os integrantes da empresa",
            description = "Paginada por pagina e tamanho, com tamanho maximo de 100. A ordem e sempre por nome,"
                    + " e o parametro de ordenacao e ignorado. busca compara com nome e e-mail.")
    public PaginaResponse<IntegranteResponse> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo,
            @ParameterObject Pageable pagina,
            IntegranteDaRequisicao quem) {
        return integranteService.listar(quem.empresaId(), new IntegranteFiltro(busca, ativo), pagina);
    }

    @GetMapping("/{integranteId}")
    @Operation(summary = "Busca um integrante da empresa")
    public IntegranteResponse buscar(@PathVariable UUID integranteId, IntegranteDaRequisicao quem) {
        return integranteService.buscar(quem.empresaId(), integranteId);
    }

    @PostMapping
    @Operation(
            summary = "Cadastra um integrante na empresa",
            description = "O e-mail e unico no sistema inteiro. O papel e INTEGRANTE ou ADMIN. A senha tem de 8"
                    + " caracteres a 72 bytes e nunca volta na resposta.")
    public ResponseEntity<IntegranteResponse> criar(
            @Valid @RequestBody IntegranteCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        IntegranteResponse resposta = integranteService.criar(quem.empresaId(), requisicao);
        return ResponseEntity.created(URI.create("/api/v1/painel/integrantes/" + resposta.id()))
                .body(resposta);
    }

    @PutMapping("/{integranteId}")
    @Operation(summary = "Altera nome, e-mail, papel e perfil do integrante")
    public IntegranteResponse alterar(
            @PathVariable UUID integranteId,
            @Valid @RequestBody IntegranteAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return integranteService.alterar(quem.empresaId(), integranteId, requisicao, quem);
    }

    @PutMapping("/{integranteId}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Define a senha do integrante",
            description = "Revoga todas as sessoes do integrante. Conta ADMIN so tem a senha definida pelo papel NATY.")
    public void definirSenha(
            @PathVariable UUID integranteId, @Valid @RequestBody SenhaRequest requisicao, IntegranteDaRequisicao quem) {
        integranteService.definirSenha(quem.empresaId(), integranteId, requisicao.senha(), quem);
    }

    @PutMapping("/{integranteId}/ativo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Ativa ou desativa o integrante",
            description = "Desativar revoga as sessoes e preserva o progresso. Integrante nunca e apagado.")
    public void definirAtivo(
            @PathVariable UUID integranteId, @Valid @RequestBody AtivoRequest requisicao, IntegranteDaRequisicao quem) {
        integranteService.definirAtivo(quem.empresaId(), integranteId, requisicao.ativo(), quem);
    }
}
