package com.projetointegrador.natysync.acompanhamento;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.JsonNode;

@TestPropertySource(properties = "app.acompanhamento.dias-parado=7")
class PrazoConfiguradoDeAcompanhamentoApiTest extends BaseDeAcompanhamentoApiTest {

    @Test
    void oPrazoVemDaPropriedadeEMudaASituacao() {
        Empresa empresa = criarEmpresa("Empresa com prazo de 7 dias");
        OffsetDateTime agora = OffsetDateTime.now();
        concluir(integrante(empresa, "Cinco dias"), 1, agora.minusDays(5));
        concluir(integrante(empresa, "Oito dias"), 1, agora.minusDays(8));

        JsonNode pagina = ler("/api/v1/acompanhamento/empresas/" + empresa.getId() + "/integrantes")
                .getBody();

        assertThat(item(pagina, "Cinco dias").get("situacao").asText()).isEqualTo("AVANCANDO");
        assertThat(item(pagina, "Oito dias").get("situacao").asText()).isEqualTo("PARADO");
    }
}
