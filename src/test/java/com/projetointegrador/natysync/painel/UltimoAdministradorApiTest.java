package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class UltimoAdministradorApiTest extends PainelTest {

    private Empresa empresa;
    private Conta naty;
    private Conta admin;

    @BeforeEach
    void criarContas() {
        naty = criarConta(criarEmpresa("Naty do Ultimo Admin"), Papel.NATY);
        empresa = criarEmpresa("Empresa do Ultimo Admin");
        admin = criarConta(empresa, Papel.ADMIN);
    }

    private String caminho(Conta conta) {
        return "/api/v1/painel/empresas/" + empresa.getId() + "/integrantes/" + conta.id();
    }

    private ResponseEntity<JsonNode> desativar(Conta alvo) {
        return chamar(naty.token(), HttpMethod.PUT, caminho(alvo) + "/ativo", Map.of("ativo", false));
    }

    private ResponseEntity<JsonNode> mudarPapel(Conta alvo, Papel papel) {
        return chamar(
                naty.token(),
                HttpMethod.PUT,
                caminho(alvo),
                Map.of("nome", "Conta Alterada", "email", alvo.email(), "papel", papel.name()));
    }

    private boolean ativo(Conta conta) {
        return jdbc.queryForObject("select ativo from usuario where id = ?", Boolean.class, conta.id());
    }

    private String papel(Conta conta) {
        return jdbc.queryForObject("select papel from usuario where id = ?", String.class, conta.id());
    }

    @Test
    void natyNaoDesativaOUnicoAdminAtivo() {
        ResponseEntity<JsonNode> resposta = desativar(admin);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ULTIMO_ADMINISTRADOR");
        assertThat(ativo(admin)).isTrue();
    }

    @Test
    void adminNaoDesativaAPropriaContaPorqueContaAdminEhDoNaty() {
        ResponseEntity<JsonNode> resposta = chamar(
                admin.token(),
                HttpMethod.PUT,
                "/api/v1/painel/integrantes/" + admin.id() + "/ativo",
                Map.of("ativo", false));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ativo(admin)).isTrue();
    }

    @Test
    void natyNaoRebaixaOUnicoAdminAtivo() {
        ResponseEntity<JsonNode> resposta = mudarPapel(admin, Papel.INTEGRANTE);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ULTIMO_ADMINISTRADOR");
        assertThat(papel(admin)).isEqualTo("ADMIN");
    }

    @Test
    void natyDesativaUmAdminHavendoOutroAtivo() {
        Conta outro = criarConta(empresa, Papel.ADMIN);

        ResponseEntity<JsonNode> resposta = desativar(admin);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(ativo(admin)).isFalse();
        assertThat(ativo(outro)).isTrue();
    }

    @Test
    void natyRebaixaUmAdminHavendoOutroAtivo() {
        criarConta(empresa, Papel.ADMIN);

        ResponseEntity<JsonNode> resposta = mudarPapel(admin, Papel.INTEGRANTE);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(papel(admin)).isEqualTo("INTEGRANTE");
    }

    @Test
    void outroAdminJaDesativadoNaoConta() {
        Conta inativo = criarConta(empresa, Papel.ADMIN);
        jdbc.update("update usuario set ativo = false where id = ?", inativo.id());

        ResponseEntity<JsonNode> resposta = desativar(admin);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ULTIMO_ADMINISTRADOR");
        assertThat(ativo(admin)).isTrue();
    }

    @Test
    void desativarContaIntegranteNaoPassaPelaRegra() {
        Conta integrante = criarConta(empresa, Papel.INTEGRANTE);

        assertThat(desativar(integrante).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void desativarAEmpresaInteiraComUmUnicoAdminEhPermitido() {
        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.PUT,
                "/api/v1/painel/empresas/" + empresa.getId(),
                CorpoDeEmpresa.de("Empresa do Ultimo Admin", false));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("ativa").asBoolean()).isFalse();
    }

    @Test
    void desativacoesConcorrentesDosDoisUltimosAdminsNaoDeixamAEmpresaSemNenhum() throws Exception {
        Conta segundo = criarConta(empresa, Papel.ADMIN);
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Integer> status = new ArrayList<>();
        try {
            List<Future<Integer>> resultados = new ArrayList<>();
            for (Conta alvo : List.of(admin, segundo)) {
                resultados.add(executor.submit(() -> {
                    largada.await();
                    return desativar(alvo).getStatusCode().value();
                }));
            }
            largada.countDown();
            for (Future<Integer> resultado : resultados) {
                status.add(resultado.get());
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(status).containsExactlyInAnyOrder(204, 409);
        assertThat(jdbc.queryForObject(
                        "select count(*) from usuario where empresa_id = ? and papel = 'ADMIN' and ativo",
                        Long.class,
                        UUID.fromString(empresa.getId().toString())))
                .isEqualTo(1);
    }
}
