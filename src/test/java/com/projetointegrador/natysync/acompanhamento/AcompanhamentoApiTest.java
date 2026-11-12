package com.projetointegrador.natysync.acompanhamento;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class AcompanhamentoApiTest extends BaseDeAcompanhamentoApiTest {

    private Empresa empresa;
    private Empresa outraEmpresa;
    private UUID ana;
    private UUID bruno;
    private UUID carla;
    private UUID dora;

    @BeforeEach
    void criarCenario() {
        empresa = criarEmpresa("Empresa Acompanhada");
        outraEmpresa = criarEmpresa("Empresa Vizinha");
        OffsetDateTime agora = OffsetDateTime.now();

        ana = integrante(empresa, "Ana");
        concluir(ana, 1, agora.minusDays(1));
        bruno = integrante(empresa, "Bruno");
        concluir(bruno, 1, agora.minusDays(10));
        carla = integrante(empresa, "Carla");
        dora = integrante(empresa, "Dora");
        concluir(dora, 6, agora.minusDays(20));

        UUID eva = integrante(empresa, "Eva");
        concluir(eva, 3, agora.minusDays(1));
        desativar(eva);
        criarIntegrante(empresa, "Conta Naty da empresa", emailUnico("naty-na-empresa"), Papel.NATY);
        integrante(outraEmpresa, "Zeca");
    }

    private String base() {
        return "/api/v1/acompanhamento/empresas/" + empresa.getId();
    }

    @Test
    void listaOsQuatroEmOrdemDeSituacaoComOsDadosDeCadaUm() {
        ResponseEntity<JsonNode> resposta = ler(base() + "/integrantes");

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        JsonNode pagina = resposta.getBody();
        assertThat(nomes(pagina)).containsExactly("Bruno", "Carla", "Ana", "Dora");
        JsonNode bruno = item(pagina, "Bruno");
        assertThat(bruno.get("situacao").asText()).isEqualTo("PARADO");
        assertThat(bruno.get("atividadesConcluidas").asInt()).isEqualTo(1);
        assertThat(bruno.get("totalAtividades").asInt()).isEqualTo(6);
        assertThat(bruno.get("percentualConcluido").asInt()).isEqualTo(16);
        assertThat(bruno.get("diasSemConcluir").asLong()).isEqualTo(10);
        assertThat(bruno.get("atividadeAtual").get("atividadeId").asText())
                .isEqualTo(ATIVIDADES.get(1).toString());
        assertThat(item(pagina, "Carla").get("situacao").asText()).isEqualTo("SEM_INICIO");
        assertThat(item(pagina, "Carla").get("ultimaConclusaoEm").isNull()).isTrue();
        assertThat(item(pagina, "Ana").get("situacao").asText()).isEqualTo("AVANCANDO");
        JsonNode dora = item(pagina, "Dora");
        assertThat(dora.get("situacao").asText()).isEqualTo("CONCLUIU");
        assertThat(dora.get("atividadeAtual").isNull()).isTrue();
        assertThat(dora.get("percentualConcluido").asInt()).isEqualTo(100);
    }

    @Test
    void outraEmpresaDesativadoENatyFicamFora() {
        JsonNode pagina = ler(base() + "/integrantes").getBody();

        assertThat(nomes(pagina)).doesNotContain("Zeca", "Eva", "Conta Naty da empresa");
        assertThat(pagina.get("totalItens").asInt()).isEqualTo(4);
    }

    @Test
    void filtroPorSituacaoTrazSoAquelaSituacao() {
        JsonNode pagina = ler(base() + "/integrantes?situacao=PARADO").getBody();

        assertThat(nomes(pagina)).containsExactly("Bruno");
        assertThat(pagina.get("totalItens").asInt()).isEqualTo(1);
    }

    @Test
    void situacaoInvalidaEhRequisicaoMalformada() {
        ResponseEntity<JsonNode> resposta = ler(base() + "/integrantes?situacao=XYZ");

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("REQUISICAO_MALFORMADA");
    }

    @Test
    void paginaRespeitaTamanhoEOrdemEhEstavelEntrePaginas() {
        JsonNode primeira = ler(base() + "/integrantes?pagina=0&tamanho=2").getBody();
        JsonNode segunda = ler(base() + "/integrantes?pagina=1&tamanho=2").getBody();

        assertThat(nomes(primeira)).containsExactly("Bruno", "Carla");
        assertThat(nomes(segunda)).containsExactly("Ana", "Dora");
        assertThat(primeira.get("totalItens").asInt()).isEqualTo(4);
        assertThat(primeira.get("totalPaginas").asInt()).isEqualTo(2);
    }

    @Test
    void tamanhoAcimaDoMaximoEhLimitadoACem() {
        JsonNode pagina = ler(base() + "/integrantes?tamanho=500").getBody();

        assertThat(pagina.get("tamanho").asInt()).isEqualTo(100);
    }

    @Test
    void empresaInexistenteEhNaoEncontrada() {
        for (String rota : new String[] {"/integrantes", "/metricas", "/integrantes.csv"}) {
            ResponseEntity<JsonNode> resposta = ler("/api/v1/acompanhamento/empresas/" + UUID.randomUUID() + rota);

            assertThat(resposta.getStatusCode().value()).as(rota).isEqualTo(404);
        }
    }

    @Test
    void metricasTemUmIntegranteEmCadaSituacao() {
        JsonNode metricas = ler(base() + "/metricas").getBody();

        assertThat(metricas.get("empresaId").asText()).isEqualTo(empresa.getId().toString());
        assertThat(metricas.get("totalIntegrantes").asInt()).isEqualTo(4);
        assertThat(metricas.get("adesaoPercentual").asInt()).isEqualTo(75);
        assertThat(metricas.get("percentualMedioDeConclusao").asInt()).isEqualTo(33);
        assertThat(metricas.get("concluiram").asInt()).isEqualTo(1);
        assertThat(metricas.get("avancando").asInt()).isEqualTo(1);
        assertThat(metricas.get("parados").asInt()).isEqualTo(1);
        assertThat(metricas.get("semInicio").asInt()).isEqualTo(1);
    }

    @Test
    void asContagensDasMetricasCoincidemComAListagem() {
        JsonNode metricas = ler(base() + "/metricas").getBody();

        for (String situacao : new String[] {"PARADO", "AVANCANDO", "SEM_INICIO", "CONCLUIU"}) {
            int naListagem = ler(base() + "/integrantes?situacao=" + situacao)
                    .getBody()
                    .get("totalItens")
                    .asInt();
            String campo =
                    switch (situacao) {
                        case "PARADO" -> "parados";
                        case "AVANCANDO" -> "avancando";
                        case "SEM_INICIO" -> "semInicio";
                        default -> "concluiram";
                    };
            assertThat(metricas.get(campo).asInt()).as(situacao).isEqualTo(naListagem);
        }
    }

    @Test
    void empresaSemIntegrantesTemMetricasZeradas() {
        Empresa vazia = criarEmpresa("Empresa vazia");

        JsonNode metricas = ler("/api/v1/acompanhamento/empresas/" + vazia.getId() + "/metricas")
                .getBody();

        assertThat(metricas.get("totalIntegrantes").asInt()).isZero();
        assertThat(metricas.get("adesaoPercentual").asInt()).isZero();
        assertThat(metricas.get("percentualMedioDeConclusao").asInt()).isZero();
        assertThat(metricas.get("parados").asInt()).isZero();
    }

    @Test
    void funilDaEmpresaMostraOGargalo() {
        ResponseEntity<JsonNode> resposta = ler("/api/v1/acompanhamento/atividades?empresaId=" + empresa.getId());

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        JsonNode primeira = atividade(resposta.getBody(), ATIVIDADES.get(0));
        JsonNode segunda = atividade(resposta.getBody(), ATIVIDADES.get(1));
        JsonNode terceira = atividade(resposta.getBody(), ATIVIDADES.get(2));
        assertThat(primeira.get("chegaram").asInt()).isEqualTo(4);
        assertThat(primeira.get("passaram").asInt()).isEqualTo(3);
        assertThat(primeira.get("percentualDePassagem").asInt()).isEqualTo(75);
        assertThat(segunda.get("chegaram").asInt()).isEqualTo(3);
        assertThat(segunda.get("passaram").asInt()).isEqualTo(1);
        assertThat(segunda.get("percentualDePassagem").asInt()).isEqualTo(33);
        assertThat(terceira.get("chegaram").asInt()).isEqualTo(1);
        assertThat(terceira.get("passaram").asInt()).isEqualTo(1);
    }

    @Test
    void funilNaoContaDesativadoNemNaty() {
        JsonNode primeira = atividade(
                ler("/api/v1/acompanhamento/atividades?empresaId=" + empresa.getId())
                        .getBody(),
                ATIVIDADES.get(0));

        assertThat(primeira.get("chegaram").asInt()).isEqualTo(4);
        assertThat(primeira.get("passaram").asInt()).isEqualTo(3);
    }

    @Test
    void funilSemFiltroSomaTodasAsEmpresas() {
        JsonNode primeira = atividade(ler("/api/v1/acompanhamento/atividades").getBody(), ATIVIDADES.get(0));

        Integer consideradosNoBanco =
                jdbc.queryForObject("select count(*) from usuario where ativo and papel <> 'NATY'", Integer.class);
        assertThat(primeira.get("chegaram").asInt()).isEqualTo(consideradosNoBanco);
        assertThat(primeira.get("passaram").asInt()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void funilDeEmpresaInexistenteEhNaoEncontrado() {
        ResponseEntity<JsonNode> resposta = ler("/api/v1/acompanhamento/atividades?empresaId=" + UUID.randomUUID());

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void funilVemNaOrdemDasAtividades() {
        JsonNode funil = ler("/api/v1/acompanhamento/atividades?empresaId=" + empresa.getId())
                .getBody();

        for (int i = 0; i < ATIVIDADES.size(); i++) {
            assertThat(funil.get(i).get("atividadeId").asText())
                    .isEqualTo(ATIVIDADES.get(i).toString());
        }
    }

    private JsonNode atividade(JsonNode funil, UUID atividadeId) {
        for (JsonNode item : funil) {
            if (item.get("atividadeId").asText().equals(atividadeId.toString())) {
                return item;
            }
        }
        throw new AssertionError("Atividade fora do funil: " + atividadeId);
    }
}
