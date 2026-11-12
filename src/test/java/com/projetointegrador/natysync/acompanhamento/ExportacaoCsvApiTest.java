package com.projetointegrador.natysync.acompanhamento;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class ExportacaoCsvApiTest extends BaseDeAcompanhamentoApiTest {

    private Empresa empresa;

    @BeforeEach
    void criarCenario() {
        empresa = criarEmpresa("Empresa do CSV");
        OffsetDateTime agora = OffsetDateTime.now();
        concluir(integrante(empresa, "Bruno"), 1, agora.minusDays(10));
        integrante(empresa, "Carla");
        concluir(integrante(empresa, "Ana"), 1, agora.minusDays(1));
    }

    private ResponseEntity<String> baixar(String empresaId, String consulta) {
        return cliente(tokenNaty)
                .get()
                .uri("/api/v1/acompanhamento/empresas/" + empresaId + "/integrantes.csv" + consulta)
                .retrieve()
                .toEntity(String.class);
    }

    private List<String> linhas(String csv) {
        return Arrays.asList(csv.split("\r\n"));
    }

    @Test
    void trazCabecalhoEUmaLinhaPorIntegranteNaOrdemDaListagem() {
        ResponseEntity<String> resposta = baixar(empresa.getId().toString(), "");

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        List<String> linhas = linhas(resposta.getBody());
        assertThat(linhas).hasSize(4);
        assertThat(linhas.get(0))
                .isEqualTo("﻿nome,email,papel,situacao,atividades_concluidas,total_atividades,"
                        + "percentual_concluido,atividade_atual,ultima_conclusao,dias_sem_concluir");
        assertThat(linhas.get(1)).startsWith("Bruno,").contains(",PARADO,1,6,16,");
        assertThat(linhas.get(2)).startsWith("Carla,").contains(",SEM_INICIO,0,6,0,");
        assertThat(linhas.get(3)).startsWith("Ana,").contains(",AVANCANDO,1,6,16,");
    }

    @Test
    void cabecalhosHttpIndicamCsvEAnexoComNomeDerivadoDoIdentificador() {
        ResponseEntity<String> resposta = baixar(empresa.getId().toString(), "");

        assertThat(resposta.getHeaders().getContentType().toString())
                .startsWith("text/csv")
                .containsIgnoringCase("utf-8");
        assertThat(resposta.getHeaders().getFirst("Content-Disposition"))
                .startsWith("attachment")
                .contains("integrantes-" + empresa.getId() + ".csv");
    }

    @Test
    void filtroPorSituacaoValeNoCsv() {
        List<String> linhas =
                linhas(baixar(empresa.getId().toString(), "?situacao=PARADO").getBody());

        assertThat(linhas).hasSize(2);
        assertThat(linhas.get(1)).startsWith("Bruno,");
    }

    @Test
    void nomeQueParecesFormulaGanhaApostrofoENomeComVirgulaVaiEntreAspas() {
        Empresa outra = criarEmpresa("Empresa de nomes perigosos");
        integrante(outra, "=cmd()");
        integrante(outra, "Souza, \"Bia\"");

        List<String> linhas = linhas(baixar(outra.getId().toString(), "").getBody());

        assertThat(linhas).hasSize(3);
        assertThat(linhas.get(1)).startsWith("'=cmd(),");
        assertThat(linhas.get(2)).startsWith("\"Souza, \"\"Bia\"\"\",");
    }

    @Test
    void empresaSemIntegrantesDevolveSoOCabecalho() {
        Empresa vazia = criarEmpresa("Empresa vazia do CSV");

        List<String> linhas = linhas(baixar(vazia.getId().toString(), "").getBody());

        assertThat(linhas).hasSize(1);
    }

    @Test
    void empresaInexistenteEhNaoEncontrada() {
        ResponseEntity<String> resposta = baixar(UUID.randomUUID().toString(), "");

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
    }
}
