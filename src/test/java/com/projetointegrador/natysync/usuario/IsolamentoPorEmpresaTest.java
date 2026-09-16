package com.projetointegrador.natysync.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class IsolamentoPorEmpresaTest extends IntegracaoTest {

    private static final UUID TRILHA = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PRIMEIRA_ATIVIDADE = UUID.fromString("00000000-0000-0000-0002-000000000001");

    private UUID empresaAId;
    private UUID empresaBId;
    private UUID integranteAId;
    private UUID integranteBId;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void criarDuasEmpresas() {
        Empresa empresaA = criarEmpresa("Empresa A");
        empresaAId = empresaA.getId();
        integranteAId = criarIntegrante(empresaA, "Integrante A", "integrante-a@teste.com.br");
        tokenA = autenticar("integrante-a@teste.com.br", SENHA_DE_TESTE);

        Empresa empresaB = criarEmpresa("Empresa B");
        empresaBId = empresaB.getId();
        integranteBId = criarIntegrante(empresaB, "Integrante B", "integrante-b@teste.com.br");
        tokenB = autenticar("integrante-b@teste.com.br", SENHA_DE_TESTE);
    }

    @AfterEach
    void limpar() {
        usuarioRepository.deleteById(integranteAId);
        usuarioRepository.deleteById(integranteBId);
        empresaRepository.deleteById(empresaAId);
        empresaRepository.deleteById(empresaBId);
    }

    private JsonNode progressoCom(String token) {
        return cliente(token)
                .get()
                .uri("/api/v1/trilhas/{trilhaId}/progresso", TRILHA)
                .retrieve()
                .body(JsonNode.class);
    }

    @Test
    void cadaTokenDevolveASuaPropriaEmpresa() {
        assertThat(cliente(tokenA)
                        .get()
                        .uri("/api/v1/trilhas/{trilhaId}/progresso", TRILHA)
                        .retrieve()
                        .toEntity(JsonNode.class)
                        .getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(usuarioRepository
                        .findById(integranteAId)
                        .orElseThrow()
                        .getEmpresa()
                        .getId())
                .isEqualTo(empresaAId);
        assertThat(usuarioRepository
                        .findById(integranteBId)
                        .orElseThrow()
                        .getEmpresa()
                        .getId())
                .isEqualTo(empresaBId);
    }

    @Test
    void progressoDeUmaEmpresaNaoVazaParaOutra() {
        ResponseEntity<JsonNode> conclusao = cliente(tokenA)
                .post()
                .uri("/api/v1/atividades/{atividadeId}/video-assistido", PRIMEIRA_ATIVIDADE)
                .retrieve()
                .toEntity(JsonNode.class);
        assertThat(conclusao.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(progressoCom(tokenA).get("atividadesConcluidas").asInt()).isEqualTo(1);
        assertThat(progressoCom(tokenB).get("atividadesConcluidas").asInt()).isZero();
    }

    @Test
    @Timeout(30)
    void oConteudoContinuaIdenticoEntreEmpresas() {
        String conteudoA =
                cliente(tokenA).get().uri("/api/v1/trilhas").retrieve().body(String.class);
        String conteudoB =
                cliente(tokenB).get().uri("/api/v1/trilhas").retrieve().body(String.class);

        assertThat(conteudoA).isEqualTo(conteudoB);
    }
}
