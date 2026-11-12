package com.projetointegrador.natysync.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

class RequisicaoIdApiTest extends IntegracaoTest {

    @Value("${logging.pattern.level}")
    private String padraoDeLog;

    private String cabecalhoDe(ResponseEntity<String> resposta) {
        return resposta.getHeaders().getFirst(RequisicaoIdFiltro.CABECALHO);
    }

    @Test
    void respostaDeSucessoTrazOIdentificador() {
        ResponseEntity<String> resposta =
                clienteSemSessao().get().uri("/actuator/health").retrieve().toEntity(String.class);

        assertThat(cabecalhoDe(resposta)).isNotBlank();
    }

    @Test
    void respostaDe401TrazOIdentificador() {
        ResponseEntity<String> resposta =
                clienteSemSessao().get().uri("/api/v1/usuarios").retrieve().toEntity(String.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(401);
        assertThat(cabecalhoDe(resposta)).isNotBlank();
    }

    @Test
    void respostaDe403TrazOIdentificador() {
        ResponseEntity<String> resposta =
                cliente().get().uri("/api/v1/painel/empresas").retrieve().toEntity(String.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(403);
        assertThat(cabecalhoDe(resposta)).isNotBlank();
    }

    @Test
    void respostaDe404TrazOIdentificador() {
        ResponseEntity<String> resposta =
                cliente().get().uri("/api/v1/rota-que-nao-existe").retrieve().toEntity(String.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
        assertThat(cabecalhoDe(resposta)).isNotBlank();
    }

    @Test
    void identificadorDoClienteVoltaNaResposta() {
        ResponseEntity<String> resposta = clienteSemSessao()
                .get()
                .uri("/api/v1/usuarios")
                .header(RequisicaoIdFiltro.CABECALHO, "abc12345-def")
                .retrieve()
                .toEntity(String.class);

        assertThat(cabecalhoDe(resposta)).isEqualTo("abc12345-def");
    }

    @Test
    void padraoDeLogIncluiOIdentificador() {
        assertThat(padraoDeLog).contains("%X{" + RequisicaoIdFiltro.CHAVE_NO_MDC);
    }
}
