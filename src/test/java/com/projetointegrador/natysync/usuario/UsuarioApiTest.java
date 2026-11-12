package com.projetointegrador.natysync.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class UsuarioApiTest extends IntegracaoTest {

    private final String emailDaAna = emailUnico("ana");
    private UUID anaId;
    private UUID brunoId;
    private UUID desativadoId;
    private UUID forasteiroId;
    private String tokenDaAna;

    @BeforeEach
    void criarEmpresas() {
        Empresa empresa = criarEmpresa("Empresa Leitura");
        anaId = criarIntegrante(empresa, "Ana", emailDaAna);
        brunoId = criarIntegrante(empresa, "Bruno", emailUnico("bruno"), Papel.ADMIN);
        desativadoId = criarIntegrante(empresa, "Carlos", emailUnico("carlos"));
        Usuario desativado = usuarioRepository.findById(desativadoId).orElseThrow();
        desativado.setAtivo(false);
        usuarioRepository.save(desativado);
        Empresa outra = criarEmpresa("Empresa Vizinha");
        forasteiroId = criarIntegrante(outra, "Zeca", emailUnico("zeca"));
        tokenDaAna = autenticar(emailDaAna, SENHA_DE_TESTE);
    }

    private ResponseEntity<JsonNode> listar(String uri) {
        return cliente(tokenDaAna).get().uri(uri).retrieve().toEntity(JsonNode.class);
    }

    private List<String> ids(JsonNode corpo) {
        List<String> ids = new ArrayList<>();
        corpo.get("itens").forEach(item -> ids.add(item.get("id").asText()));
        return ids;
    }

    @Test
    void devolveColegasAtivosDaPropriaEmpresaOrdenadosPorNome() {
        ResponseEntity<JsonNode> resposta = listar("/api/v1/usuarios");

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ids(resposta.getBody())).containsExactly(anaId.toString(), brunoId.toString());
    }

    @Test
    void naoDevolveIntegranteDeOutraEmpresaNemDesativado() {
        List<String> ids = ids(listar("/api/v1/usuarios").getBody());

        assertThat(ids).doesNotContain(forasteiroId.toString(), desativadoId.toString());
    }

    @Test
    void itemTemSomenteIdNomeEPapel() {
        JsonNode item = listar("/api/v1/usuarios").getBody().get("itens").get(1);

        List<String> campos = new ArrayList<>();
        item.propertyNames().forEach(campos::add);
        assertThat(campos).containsExactlyInAnyOrder("id", "nome", "papel");
        assertThat(item.get("papel").asText()).isEqualTo("ADMIN");
    }

    @Test
    void tamanhoAcimaDoMaximoEhLimitadoACem() {
        JsonNode corpo = listar("/api/v1/usuarios?tamanho=500").getBody();

        assertThat(corpo.get("tamanho").asInt()).isEqualTo(100);
    }

    @Test
    void paginaRespeitaTamanho() {
        JsonNode corpo = listar("/api/v1/usuarios?pagina=1&tamanho=1").getBody();

        assertThat(ids(corpo)).containsExactly(brunoId.toString());
        assertThat(corpo.get("totalItens").asLong()).isEqualTo(2);
    }

    @Test
    void semTokenEhRecusado() {
        ResponseEntity<JsonNode> resposta =
                clienteSemSessao().get().uri("/api/v1/usuarios").retrieve().toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }
}
