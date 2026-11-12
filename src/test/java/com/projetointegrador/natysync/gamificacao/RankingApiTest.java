package com.projetointegrador.natysync.gamificacao;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class RankingApiTest extends GamificacaoTest {

    private static final OffsetDateTime REFERENCIA = OffsetDateTime.of(2026, 10, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    private Empresa empresa;

    @BeforeEach
    void preparar() {
        empresa = criarEmpresa("Empresa ranking");
    }

    @Test
    void ordenaPorPontosEDesempataPorQuemChegouPrimeiro() {
        Conta trinta = novaConta(empresa, "Zeca");
        Conta vinteTarde = novaConta(empresa, "Amanda");
        Conta vinteCedo = novaConta(empresa, "Yara");
        pontuar(trinta, 3, REFERENCIA);
        pontuar(vinteTarde, 2, REFERENCIA.plusHours(2));
        pontuar(vinteCedo, 2, REFERENCIA.plusHours(1));

        JsonNode itens = ranking(trinta.token(), "").getBody().get("itens");

        assertThat(nomes(itens)).containsExactly("Zeca", "Yara", "Amanda");
        assertThat(itens.get(0).get("pontos").asLong()).isEqualTo(30);
        assertThat(itens.get(0).get("integranteId").asText())
                .isEqualTo(trinta.id().toString());
    }

    @Test
    void contaSemPontoVemNoFimPorNomeInclusiveAdmin() {
        Conta pontuada = novaConta(empresa, "Carla");
        novaConta(empresa, "Bruna");
        novaConta(empresa, "Ana", Papel.ADMIN);
        pontuar(pontuada, 2, REFERENCIA);

        JsonNode itens = ranking(pontuada.token(), "").getBody().get("itens");

        assertThat(nomes(itens)).containsExactly("Carla", "Ana", "Bruna");
        assertThat(itens.get(1).get("pontos").asLong()).isZero();
    }

    @Test
    void desativadoEOutraEmpresaNaoAparecem() {
        Conta quemChama = novaConta(empresa, "Quem Chama");
        Conta desativado = novaConta(empresa, "Desativado");
        pontuar(desativado, 3, REFERENCIA);
        jdbc.update("update usuario set ativo = false where id = ?", desativado.id());
        Conta vizinho = novaConta(criarEmpresa("Empresa vizinha"), "Vizinho");
        pontuar(vizinho, 5, REFERENCIA);

        JsonNode corpo = ranking(quemChama.token(), "").getBody();

        assertThat(nomes(corpo.get("itens"))).containsExactly("Quem Chama");
        assertThat(corpo.get("totalItens").asLong()).isEqualTo(1);
        assertThat(pontosConcedidos(desativado)).isEqualTo(3);
    }

    @Test
    void paginaComPosicaoOrdinalContinuaEntrePaginas() {
        List<Conta> contas = new ArrayList<>();
        for (int indice = 0; indice < 5; indice++) {
            Conta conta = novaConta(empresa, "Integrante " + indice);
            pontuar(conta, 2, REFERENCIA.plusMinutes(indice));
            contas.add(conta);
        }

        JsonNode segunda =
                ranking(contas.getFirst().token(), "?pagina=1&tamanho=2").getBody();

        assertThat(segunda.get("itens").get(0).get("posicao").asLong()).isEqualTo(3);
        assertThat(segunda.get("itens").get(1).get("posicao").asLong()).isEqualTo(4);
        assertThat(segunda.get("pagina").asInt()).isEqualTo(1);
        assertThat(segunda.get("tamanho").asInt()).isEqualTo(2);
        assertThat(segunda.get("totalItens").asLong()).isEqualTo(5);
        assertThat(segunda.get("totalPaginas").asInt()).isEqualTo(3);
        assertThat(nomes(segunda.get("itens"))).containsExactly("Integrante 2", "Integrante 3");
    }

    @Test
    void tamanhoPadraoMaximoEOrdemDoClienteIgnorada() {
        Conta zeca = novaConta(empresa, "Zeca");
        Conta ana = novaConta(empresa, "Ana");
        pontuar(zeca, 2, REFERENCIA);

        assertThat(ranking(zeca.token(), "").getBody().get("tamanho").asInt()).isEqualTo(20);
        assertThat(ranking(zeca.token(), "?tamanho=500")
                        .getBody()
                        .get("tamanho")
                        .asInt())
                .isEqualTo(100);
        assertThat(nomes(ranking(ana.token(), "?sort=nome,asc").getBody().get("itens")))
                .containsExactly("Zeca", "Ana");
    }

    @Test
    void semTokenResponde401() {
        ResponseEntity<JsonNode> resposta =
                clienteSemSessao().get().uri("/api/v1/ranking").retrieve().toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }

    private void pontuar(Conta conta, int atividades, OffsetDateTime ultimaConcessao) {
        for (int indice = 0; indice < atividades; indice++) {
            UUID atividadeId = ATIVIDADES_DA_TRILHA.get(indice);
            jdbc.update(
                    "insert into ponto_concedido (usuario_id, atividade_id, xp, concedido_em) values (?, ?, 10, ?)",
                    conta.id(),
                    atividadeId,
                    ultimaConcessao.minusMinutes(atividades - 1 - indice));
        }
    }

    private ResponseEntity<JsonNode> ranking(String token, String consulta) {
        return cliente(token).get().uri("/api/v1/ranking" + consulta).retrieve().toEntity(JsonNode.class);
    }

    private List<String> nomes(JsonNode itens) {
        List<String> nomes = new ArrayList<>();
        itens.forEach(item -> nomes.add(item.get("nome").asText()));
        return nomes;
    }
}
