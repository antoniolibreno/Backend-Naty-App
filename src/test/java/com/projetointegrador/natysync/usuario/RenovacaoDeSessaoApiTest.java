package com.projetointegrador.natysync.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class RenovacaoDeSessaoApiTest extends IntegracaoTest {

    private static final String RENOVACAO = "/api/v1/sessoes/atual/renovacao";
    private static final Duration DURACAO_MAXIMA = Duration.ofDays(30);

    private final String email = emailUnico("renovacao");
    private Empresa empresa;
    private UUID integranteId;

    @BeforeEach
    void criarIntegrante() {
        empresa = criarEmpresa("Empresa Renovacao");
        integranteId = criarIntegrante(empresa, "Integrante Renovacao", email);
    }

    private ResponseEntity<JsonNode> renovar(String token) {
        var cliente = token == null ? clienteSemSessao() : cliente(token);
        return cliente.post().uri(RENOVACAO).retrieve().toEntity(JsonNode.class);
    }

    private int statusDeUmaChamada(String token) {
        return cliente(token)
                .get()
                .uri("/api/v1/usuarios")
                .retrieve()
                .toBodilessEntity()
                .getStatusCode()
                .value();
    }

    private String hashDe(String token) throws Exception {
        return HexFormat.of()
                .formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    }

    private String sessaoComDatas(OffsetDateTime iniciadaEm, OffsetDateTime expiraEm) throws Exception {
        String token = "token-de-teste-" + UUID.randomUUID();
        jdbc.update(
                "insert into sessao (id, usuario_id, token_hash, criado_em, iniciada_em, expira_em)"
                        + " values (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                integranteId,
                hashDe(token),
                iniciadaEm,
                iniciadaEm,
                expiraEm);
        return token;
    }

    @Test
    void renovaComTokenValidoEEmiteTokenAceito() {
        String antigo = autenticar(email, SENHA_DE_TESTE);

        ResponseEntity<JsonNode> resposta = renovar(antigo);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        String novo = resposta.getBody().get("token").asText();
        assertThat(novo).isNotBlank().isNotEqualTo(antigo);
        assertThat(OffsetDateTime.parse(resposta.getBody().get("expiraEm").asText()))
                .isAfter(OffsetDateTime.now());
        assertThat(resposta.getBody().get("usuarioId").asText()).isEqualTo(integranteId.toString());
        assertThat(resposta.getBody().get("papel").asText()).isEqualTo("INTEGRANTE");
        assertThat(statusDeUmaChamada(novo)).isEqualTo(200);
    }

    @Test
    void tokenApresentadoNaRenovacaoDeixaDeValer() {
        String antigo = autenticar(email, SENHA_DE_TESTE);
        renovar(antigo);

        assertThat(statusDeUmaChamada(antigo)).isEqualTo(401);
        ResponseEntity<JsonNode> segunda = renovar(antigo);
        assertThat(segunda.getStatusCode().value()).isEqualTo(401);
        assertThat(segunda.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }

    @Test
    void expiracaoFicaLimitadaPelaDuracaoMaxima() throws Exception {
        OffsetDateTime iniciadaEm = OffsetDateTime.now().minus(DURACAO_MAXIMA).plusDays(1);
        String token = sessaoComDatas(iniciadaEm, OffsetDateTime.now().plusHours(1));

        ResponseEntity<JsonNode> resposta = renovar(token);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        OffsetDateTime expiraEm =
                OffsetDateTime.parse(resposta.getBody().get("expiraEm").asText());
        assertThat(expiraEm)
                .isBetween(
                        iniciadaEm.plus(DURACAO_MAXIMA).minusSeconds(1),
                        iniciadaEm.plus(DURACAO_MAXIMA).plusSeconds(1));
    }

    @Test
    void renovacaoHerdaOInicioDoLogin() throws Exception {
        OffsetDateTime iniciadaEm = OffsetDateTime.now().minusDays(10).truncatedTo(ChronoUnit.MICROS);
        String token = sessaoComDatas(iniciadaEm, OffsetDateTime.now().plusHours(1));

        String novo = renovar(token).getBody().get("token").asText();

        OffsetDateTime iniciadaEmDaNova = jdbc.queryForObject(
                "select iniciada_em from sessao where token_hash = ?", OffsetDateTime.class, hashDe(novo));
        assertThat(iniciadaEmDaNova).isEqualTo(iniciadaEm.withOffsetSameInstant(iniciadaEmDaNova.getOffset()));
    }

    @Test
    void duracaoMaximaEsgotadaExigeNovoLogin() throws Exception {
        String token = sessaoComDatas(
                OffsetDateTime.now().minus(DURACAO_MAXIMA).minusDays(1),
                OffsetDateTime.now().plusHours(1));

        ResponseEntity<JsonNode> resposta = renovar(token);

        assertThat(resposta.getStatusCode().value()).isEqualTo(401);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }

    @Test
    void tokenExpiradoNaoRenova() throws Exception {
        String token = sessaoComDatas(
                OffsetDateTime.now().minusDays(8), OffsetDateTime.now().minusHours(1));

        ResponseEntity<JsonNode> resposta = renovar(token);

        assertThat(resposta.getStatusCode().value()).isEqualTo(401);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }

    @Test
    void tokenRevogadoNaoRenova() {
        String token = autenticar(email, SENHA_DE_TESTE);
        cliente(token).delete().uri("/api/v1/sessoes/atual").retrieve().toBodilessEntity();

        assertThat(renovar(token).getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void integranteDesativadoNaoRenova() {
        String token = autenticar(email, SENHA_DE_TESTE);
        Usuario usuario = usuarioRepository.findById(integranteId).orElseThrow();
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        assertThat(renovar(token).getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void semTokenEhRecusado() {
        ResponseEntity<JsonNode> resposta = renovar(null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(401);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }

    @Test
    void renovacoesConcorrentesEmitemNoMaximoUmToken() throws Exception {
        String token = autenticar(email, SENHA_DE_TESTE);
        int chamadas = 4;
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(chamadas);
        try {
            List<Future<Integer>> resultados = new ArrayList<>();
            for (int i = 0; i < chamadas; i++) {
                Callable<Integer> chamada = () -> {
                    largada.await();
                    return renovar(token).getStatusCode().value();
                };
                resultados.add(executor.submit(chamada));
            }
            largada.countDown();
            List<Integer> status = new ArrayList<>();
            for (Future<Integer> resultado : resultados) {
                status.add(resultado.get());
            }
            assertThat(status).filteredOn(codigo -> codigo == 200).hasSize(1);
            assertThat(status).filteredOn(codigo -> codigo == 401).hasSize(chamadas - 1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void trocaDeSenhaRevogaOTokenRenovado() {
        String administrador = emailUnico("admin");
        criarIntegrante(empresa, "Administrador", administrador, Papel.ADMIN);
        String tokenDoAdministrador = autenticar(administrador, SENHA_DE_TESTE);
        String renovado = renovar(autenticar(email, SENHA_DE_TESTE))
                .getBody()
                .get("token")
                .asText();
        assertThat(statusDeUmaChamada(renovado)).isEqualTo(200);

        ResponseEntity<Void> troca = cliente(tokenDoAdministrador)
                .put()
                .uri("/api/v1/painel/integrantes/{id}/senha", integranteId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("senha", "senha-nova-12345"))
                .retrieve()
                .toBodilessEntity();

        assertThat(troca.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(statusDeUmaChamada(renovado)).isEqualTo(401);
    }
}
