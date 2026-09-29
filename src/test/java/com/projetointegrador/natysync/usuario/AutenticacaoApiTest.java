package com.projetointegrador.natysync.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class AutenticacaoApiTest extends IntegracaoTest {

    private static final UUID TRILHA = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private SessaoRepository sessaoRepository;

    private UUID empresaId;
    private UUID integranteId;
    private UUID desativadoId;

    @BeforeEach
    void criarIntegrantes() {
        Empresa empresa = criarEmpresa("Empresa de Autenticacao");
        empresaId = empresa.getId();
        integranteId = criarIntegrante(empresa, "Integrante Ativo", "ativo@teste.com.br");
        desativadoId = criarIntegrante(empresa, "Integrante Desativado", "desativado@teste.com.br");

        Usuario desativado = usuarioRepository.findById(desativadoId).orElseThrow();
        desativado.setAtivo(false);
        usuarioRepository.save(desativado);
    }

    @AfterEach
    void limpar() {
        usuarioRepository.deleteById(integranteId);
        usuarioRepository.deleteById(desativadoId);
        empresaRepository.deleteById(empresaId);
    }

    private ResponseEntity<JsonNode> autenticarCom(String email, String senha) {
        return clienteSemSessao()
                .post()
                .uri("/api/v1/sessoes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "senha", senha))
                .retrieve()
                .toEntity(JsonNode.class);
    }

    private ResponseEntity<JsonNode> lerTrilhaCom(String token) {
        return cliente(token).get().uri("/api/v1/trilhas").retrieve().toEntity(JsonNode.class);
    }

    @Test
    void senhaCorretaEmiteToken() {
        ResponseEntity<JsonNode> resposta = autenticarCom("ativo@teste.com.br", SENHA_DE_TESTE);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("token").asText()).isNotBlank();
    }

    @Test
    void senhaIncorretaEhRecusada() {
        ResponseEntity<JsonNode> resposta = autenticarCom("ativo@teste.com.br", "senha-errada");

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }

    @Test
    void emailInexistenteDevolveAMesmaRecusaQueSenhaIncorreta() {
        ResponseEntity<JsonNode> inexistente = autenticarCom("ninguem@teste.com.br", SENHA_DE_TESTE);
        ResponseEntity<JsonNode> senhaErrada = autenticarCom("ativo@teste.com.br", "senha-errada");

        assertThat(inexistente.getStatusCode()).isEqualTo(senhaErrada.getStatusCode());
        assertThat(inexistente.getBody().get("codigo").asText())
                .isEqualTo(senhaErrada.getBody().get("codigo").asText());
        assertThat(inexistente.getBody().get("mensagem").asText())
                .isEqualTo(senhaErrada.getBody().get("mensagem").asText());
    }

    @Test
    void integranteDesativadoNaoObtemSessao() {
        ResponseEntity<JsonNode> resposta = autenticarCom("desativado@teste.com.br", SENHA_DE_TESTE);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(sessaoRepository.findByUsuarioId(desativadoId)).isEmpty();
    }

    @Test
    void chamadaSemTokenEhRecusada() {
        ResponseEntity<JsonNode> resposta =
                clienteSemSessao().get().uri("/api/v1/trilhas").retrieve().toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }

    @Test
    void tokenDesconhecidoEhRecusado() {
        assertThat(lerTrilhaCom("token-que-nunca-foi-emitido").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void tokenExpiradoEhRecusado() {
        String token = autenticar("ativo@teste.com.br", SENHA_DE_TESTE);
        assertThat(lerTrilhaCom(token).getStatusCode()).isEqualTo(HttpStatus.OK);

        Sessao sessao = sessaoRepository.findByUsuarioId(integranteId).getFirst();
        sessao.setExpiraEm(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1));
        sessaoRepository.save(sessao);

        assertThat(lerTrilhaCom(token).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void tokenRevogadoNaoEhAceitoNaChamadaSeguinte() {
        String token = autenticar("ativo@teste.com.br", SENHA_DE_TESTE);
        assertThat(lerTrilhaCom(token).getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> revogacao =
                cliente(token).delete().uri("/api/v1/sessoes/atual").retrieve().toBodilessEntity();

        assertThat(revogacao.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(lerTrilhaCom(token).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void oTokenEmClaroNaoEhGuardadoEmBanco() {
        String token = autenticar("ativo@teste.com.br", SENHA_DE_TESTE);

        Sessao sessao = sessaoRepository.findByUsuarioId(integranteId).getFirst();

        assertThat(sessao.getTokenHash()).isNotEqualTo(token);
        assertThat(sessao.getTokenHash()).doesNotContain(token);
    }

    @Test
    void progressoExigeTokenAssimComoOConteudo() {
        ResponseEntity<JsonNode> resposta = clienteSemSessao()
                .get()
                .uri("/api/v1/trilhas/{trilhaId}/progresso", TRILHA)
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void integranteDeEmpresaInativaNaoAutenticaComAMesmaRecusa() {
        Empresa inativa = criarEmpresa("Empresa Inativa");
        criarIntegrante(inativa, "Integrante de Inativa", "de-inativa@teste.com.br");
        inativa.setAtiva(false);
        empresaRepository.save(inativa);

        ResponseEntity<JsonNode> recusa = autenticarCom("de-inativa@teste.com.br", SENHA_DE_TESTE);
        ResponseEntity<JsonNode> senhaErrada = autenticarCom("ativo@teste.com.br", "senha-errada");

        assertThat(recusa.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(recusa.getBody().get("mensagem").asText())
                .isEqualTo(senhaErrada.getBody().get("mensagem").asText());
    }

    @Test
    void sessaoAbertaCaiQuandoAEmpresaFicaInativa() {
        Empresa empresa = criarEmpresa("Empresa que Fica Inativa");
        criarIntegrante(empresa, "Integrante", "fica-inativa@teste.com.br");
        String token = autenticar("fica-inativa@teste.com.br", SENHA_DE_TESTE);
        assertThat(lerTrilhaCom(token).getStatusCode()).isEqualTo(HttpStatus.OK);

        empresa.setAtiva(false);
        empresaRepository.save(empresa);

        assertThat(lerTrilhaCom(token).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void usoDaSessaoRegistraOUltimoAcessoNaSessao() {
        String token = autenticar("ativo@teste.com.br", SENHA_DE_TESTE);
        Sessao sessao = sessaoRepository.findByUsuarioId(integranteId).getFirst();
        sessao.setUltimoAcessoEm(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        sessaoRepository.save(sessao);

        assertThat(lerTrilhaCom(token).getStatusCode()).isEqualTo(HttpStatus.OK);

        Sessao lida = sessaoRepository.findById(sessao.getId()).orElseThrow();
        assertThat(lida.getUltimoAcessoEm())
                .isAfter(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1));
        assertThat(lida.getRevogadoEm()).isNull();
    }

    @Test
    void emailVazioEhErroDeValidacao() {
        ResponseEntity<JsonNode> resposta = autenticarCom("", SENHA_DE_TESTE);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
    }
}
