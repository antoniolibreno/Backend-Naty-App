package com.projetointegrador.natysync.gamificacao;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class PerfilGamificacaoApiTest extends GamificacaoTest {

    private Empresa empresa;
    private Conta conta;

    @BeforeEach
    void preparar() {
        empresa = criarEmpresa("Empresa perfil");
        conta = novaConta(empresa, "Integrante Perfil");
    }

    @Test
    void perfilSemEstudoVemZeradoComOCatalogoInteiroSemObtencao() {
        JsonNode perfil = perfil(conta);

        assertThat(perfil.get("integranteId").asText()).isEqualTo(conta.id().toString());
        assertThat(perfil.get("nome").asText()).isEqualTo("Integrante Perfil");
        assertThat(perfil.get("pontos").asLong()).isZero();
        assertThat(perfil.get("posicaoNoRanking").asLong()).isEqualTo(1);
        assertThat(perfil.get("sequencia").get("atual").asInt()).isZero();
        assertThat(perfil.get("sequencia").get("maior").asInt()).isZero();
        assertThat(perfil.get("sequencia").get("diaCorrenteEstudado").asBoolean())
                .isFalse();
        assertThat(perfil.get("conquistas")).hasSize(7);
        perfil.get("conquistas").forEach(conquista -> {
            assertThat(conquista.get("nome").asText()).isNotBlank();
            assertThat(conquista.get("descricao").asText()).isNotBlank();
            assertThat(conquista.get("obtidaEm").isNull()).isTrue();
        });
    }

    @Test
    void perfilComPrimeiraAtividadeObtidaEDemaisNulas() {
        jdbc.update(
                "insert into progresso_atividade (usuario_id, atividade_id, concluido_em) values (?, ?, now())",
                conta.id(),
                ATIVIDADES_DA_TRILHA.getFirst());
        reprovar(conta, ATIVIDADES_DA_TRILHA.get(1));

        JsonNode perfil = perfil(conta);

        assertThat(perfil.get("sequencia").get("atual").asInt()).isEqualTo(1);
        assertThat(perfil.get("sequencia").get("diaCorrenteEstudado").asBoolean())
                .isTrue();
        perfil.get("conquistas")
                .forEach(conquista -> assertThat(conquista.get("obtidaEm").isNull())
                        .isEqualTo(!conquista.get("codigo").asText().equals("PRIMEIRA_ATIVIDADE")));
    }

    @Test
    void posicaoDoPerfilEhAMesmaDoRanking() {
        Conta lider = novaConta(empresa, "Lider");
        concluirAte(lider, 2);
        concluirAte(conta, 1);

        JsonNode itens = cliente(conta.token())
                .get()
                .uri("/api/v1/ranking")
                .retrieve()
                .body(JsonNode.class)
                .get("itens");

        for (JsonNode item : itens) {
            UUID integranteId = UUID.fromString(item.get("integranteId").asText());
            JsonNode perfil = perfil(conta.token(), integranteId).getBody();
            assertThat(perfil.get("posicaoNoRanking").asLong())
                    .isEqualTo(item.get("posicao").asLong());
            assertThat(perfil.get("pontos").asLong())
                    .isEqualTo(item.get("pontos").asLong());
        }
        assertThat(perfil(conta).get("posicaoNoRanking").asLong()).isEqualTo(2);
    }

    @Test
    void colegaAtivoDaMesmaEmpresaEhDevolvido() {
        Conta colega = novaConta(empresa, "Colega");

        ResponseEntity<JsonNode> resposta = perfil(conta.token(), colega.id());

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("nome").asText()).isEqualTo("Colega");
    }

    @Test
    void outraEmpresaDesativadoEInexistenteRespondemOMesmo404() {
        Conta vizinho = novaConta(criarEmpresa("Empresa vizinha perfil"), "Vizinho");
        Conta desativado = novaConta(empresa, "Desativado");
        jdbc.update("update usuario set ativo = false where id = ?", desativado.id());

        for (UUID alvo : new UUID[] {vizinho.id(), desativado.id(), UUID.randomUUID()}) {
            ResponseEntity<JsonNode> resposta = perfil(conta.token(), alvo);
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("RECURSO_NAO_ENCONTRADO");
        }
    }

    @Test
    void semTokenResponde401() {
        ResponseEntity<JsonNode> resposta = clienteSemSessao()
                .get()
                .uri("/api/v1/integrantes/{id}/gamificacao", conta.id())
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }
}
