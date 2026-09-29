package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class IntegrantePainelApiTest extends PainelTest {

    private static final String INTEGRANTES = "/api/v1/painel/integrantes";

    private Empresa empresa;
    private Conta admin;

    @BeforeEach
    void criarAdmin() {
        empresa = criarEmpresa("Empresa do Painel");
        admin = criarConta(empresa, Papel.ADMIN);
    }

    private String emailNovo() {
        return "novo-" + UUID.randomUUID() + "@teste.com.br";
    }

    private ResponseEntity<JsonNode> cadastrar(String email, Papel papel) {
        return chamar(admin.token(), HttpMethod.POST, INTEGRANTES, novoIntegrante(email, papel));
    }

    private String cadastrarId(String email) {
        return cadastrar(email, Papel.INTEGRANTE).getBody().get("id").asText();
    }

    private UUID primeiraAtividade(String token) {
        JsonNode trilhas = lerTrilhas(token).getBody();
        JsonNode trilha = chamar(
                        token,
                        HttpMethod.GET,
                        "/api/v1/trilhas/" + trilhas.get(0).get("id").asText())
                .getBody();
        return UUID.fromString(
                trilha.get("modulos").get(0).get("atividades").get(0).get("id").asText());
    }

    @Test
    void cadastroCriaIntegranteQueAutenticaESemSenhaNaResposta() {
        String email = emailNovo();

        ResponseEntity<JsonNode> resposta = cadastrar(email.toUpperCase(), Papel.INTEGRANTE);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode corpo = resposta.getBody();
        assertThat(corpo.get("email").asText()).isEqualTo(email);
        assertThat(corpo.get("empresaId").asText()).isEqualTo(empresa.getId().toString());
        assertThat(corpo.get("ativo").asBoolean()).isTrue();
        assertThat(corpo.has("senha")).isFalse();
        assertThat(corpo.has("senhaHash")).isFalse();
        assertThat(corpo.toString()).doesNotContain("senha-nova-123").doesNotContain("bcrypt");
        assertThat(autenticarCom(email, "senha-nova-123").getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void emailDeOutraEmpresaEmQualquerCaixaEhConflito() {
        String email = emailNovo();
        criarIntegrante(criarEmpresa("Outra Empresa"), "Dono do E-mail", email);

        ResponseEntity<JsonNode> resposta = cadastrar(" " + email.toUpperCase() + " ", Papel.INTEGRANTE);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("EMAIL_JA_CADASTRADO");
    }

    @Test
    void papelNatyNaoEhAtribuidoPeloPainel() {
        String email = emailNovo();

        ResponseEntity<JsonNode> resposta = cadastrar(email, Papel.NATY);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
        assertThat(jdbc.queryForObject("select count(*) from usuario where email = ?", Long.class, email))
                .isZero();
    }

    @Test
    void senhaForaDoLimiteEhErroDeValidacao() {
        Map<String, Object> curta =
                Map.of("nome", "X", "email", emailNovo(), "senha", "1234567", "papel", "INTEGRANTE");
        Map<String, Object> longa =
                Map.of("nome", "X", "email", emailNovo(), "senha", "é".repeat(37), "papel", "INTEGRANTE");

        for (Map<String, Object> corpo : java.util.List.of(curta, longa)) {
            ResponseEntity<JsonNode> resposta = chamar(admin.token(), HttpMethod.POST, INTEGRANTES, corpo);
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
            assertThat(resposta.getBody().toString()).doesNotContain("1234567").doesNotContain("éé");
        }
    }

    @Test
    void redefinirSenhaRevogaSessaoEAceitaASenhaNova() {
        Conta integrante = criarConta(empresa, Papel.INTEGRANTE);

        ResponseEntity<JsonNode> resposta = chamar(
                admin.token(),
                HttpMethod.PUT,
                INTEGRANTES + "/" + integrante.id() + "/senha",
                Map.of("senha", "senha-redefinida"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(lerTrilhas(integrante.token()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(autenticarCom(integrante.email(), SENHA_DE_TESTE).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(autenticarCom(integrante.email(), "senha-redefinida").getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void desativarRevogaSessaoRecusaLoginEPreservaProgresso() {
        Conta integrante = criarConta(empresa, Papel.INTEGRANTE);
        UUID atividade = primeiraAtividade(integrante.token());
        assertThat(chamar(integrante.token(), HttpMethod.POST, "/api/v1/atividades/" + atividade + "/video-assistido")
                        .getStatusCode())
                .isEqualTo(HttpStatus.OK);

        ResponseEntity<JsonNode> resposta = chamar(
                admin.token(), HttpMethod.PUT, INTEGRANTES + "/" + integrante.id() + "/ativo", Map.of("ativo", false));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(lerTrilhas(integrante.token()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(autenticarCom(integrante.email(), SENHA_DE_TESTE).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(jdbc.queryForObject(
                        "select count(*) from progresso_atividade where usuario_id = ?", Long.class, integrante.id()))
                .isEqualTo(1L);
        assertThat(usuarioRepository.existsById(integrante.id())).isTrue();
    }

    @Test
    void reativarDevolveOLoginSemRessuscitarTokenAntigo() {
        Conta integrante = criarConta(empresa, Papel.INTEGRANTE);
        String caminho = INTEGRANTES + "/" + integrante.id() + "/ativo";
        chamar(admin.token(), HttpMethod.PUT, caminho, Map.of("ativo", false));

        chamar(admin.token(), HttpMethod.PUT, caminho, Map.of("ativo", true));

        assertThat(lerTrilhas(integrante.token()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(autenticarCom(integrante.email(), SENHA_DE_TESTE).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void adminNaoAdministraContaAdminNemAPropria() {
        Conta outroAdmin = criarConta(empresa, Papel.ADMIN);

        for (Conta alvo : new Conta[] {admin, outroAdmin}) {
            String caminho = INTEGRANTES + "/" + alvo.id();
            ResponseEntity<JsonNode> senha =
                    chamar(admin.token(), HttpMethod.PUT, caminho + "/senha", Map.of("senha", "senha-tomada-1"));
            ResponseEntity<JsonNode> ativo =
                    chamar(admin.token(), HttpMethod.PUT, caminho + "/ativo", Map.of("ativo", false));
            ResponseEntity<JsonNode> rebaixar = chamar(
                    admin.token(),
                    HttpMethod.PUT,
                    caminho,
                    Map.of("nome", "Rebaixado", "email", alvo.email(), "papel", "INTEGRANTE"));

            for (ResponseEntity<JsonNode> resposta : java.util.List.of(senha, ativo, rebaixar)) {
                assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ACESSO_NEGADO");
            }
        }
        assertThat(autenticarCom(outroAdmin.email(), SENHA_DE_TESTE).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void natyRedefineASenhaDeUmAdmin() {
        Conta naty = criarConta(criarEmpresa("Naty da Senha"), Papel.NATY);

        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.PUT,
                "/api/v1/painel/empresas/" + empresa.getId() + "/integrantes/" + admin.id() + "/senha",
                Map.of("senha", "senha-do-admin-1"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(lerTrilhas(admin.token()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(autenticarCom(admin.email(), "senha-do-admin-1").getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void alterarSemPerfilMantemOPerfilAtual() {
        String id = chamar(
                        admin.token(),
                        HttpMethod.POST,
                        INTEGRANTES,
                        Map.of(
                                "nome", "Supervisor",
                                "email", emailNovo(),
                                "senha", "senha-nova-123",
                                "papel", "INTEGRANTE",
                                "perfil", "supervisor"))
                .getBody()
                .get("id")
                .asText();

        JsonNode alterado = chamar(
                        admin.token(),
                        HttpMethod.PUT,
                        INTEGRANTES + "/" + id,
                        Map.of("nome", "Supervisor Renomeado", "email", emailNovo(), "papel", "INTEGRANTE"))
                .getBody();

        assertThat(alterado.get("perfil").asText()).isEqualTo("supervisor");
        assertThat(alterado.get("nome").asText()).isEqualTo("Supervisor Renomeado");
    }

    @Test
    void alterarTrocaDadosEVerificaEmailUnico() {
        String id = cadastrarId(emailNovo());
        String emailOcupado = emailNovo();
        cadastrarId(emailOcupado);

        ResponseEntity<JsonNode> repetido = chamar(
                admin.token(),
                HttpMethod.PUT,
                INTEGRANTES + "/" + id,
                Map.of("nome", "Nome Novo", "email", emailOcupado, "papel", "INTEGRANTE"));
        ResponseEntity<JsonNode> alterado = chamar(
                admin.token(),
                HttpMethod.PUT,
                INTEGRANTES + "/" + id,
                Map.of("nome", "Nome Novo", "email", emailNovo(), "papel", "ADMIN", "perfil", "supervisor"));

        assertThat(repetido.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(repetido.getBody().get("codigo").asText()).isEqualTo("EMAIL_JA_CADASTRADO");
        assertThat(alterado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(alterado.getBody().get("papel").asText()).isEqualTo("ADMIN");
        assertThat(alterado.getBody().get("perfil").asText()).isEqualTo("supervisor");
    }

    @Test
    void listagemPaginaFiltraEBusca() {
        String marca = UUID.randomUUID().toString().substring(0, 8);
        cadastrar("buscado-" + marca + "@teste.com.br", Papel.INTEGRANTE);
        chamar(
                admin.token(),
                HttpMethod.POST,
                INTEGRANTES,
                Map.of(
                        "nome",
                        "Zelia " + marca + "x",
                        "email",
                        emailNovo(),
                        "senha",
                        "senha-nova-123",
                        "papel",
                        "INTEGRANTE"));
        String outroId = cadastrarId(emailNovo());
        chamar(admin.token(), HttpMethod.PUT, INTEGRANTES + "/" + outroId + "/ativo", Map.of("ativo", false));

        JsonNode pagina = chamar(admin.token(), HttpMethod.GET, INTEGRANTES + "?pagina=0&tamanho=2")
                .getBody();
        JsonNode ultima = chamar(admin.token(), HttpMethod.GET, INTEGRANTES + "?pagina=1&tamanho=2")
                .getBody();
        JsonNode porNome = chamar(admin.token(), HttpMethod.GET, INTEGRANTES + "?busca=ZELIA " + marca)
                .getBody();
        JsonNode busca = chamar(admin.token(), HttpMethod.GET, INTEGRANTES + "?busca=" + marca.toUpperCase())
                .getBody();
        JsonNode inativos = chamar(admin.token(), HttpMethod.GET, INTEGRANTES + "?ativo=false")
                .getBody();
        JsonNode grande = chamar(admin.token(), HttpMethod.GET, INTEGRANTES + "?tamanho=500")
                .getBody();

        assertThat(pagina.get("itens").size()).isEqualTo(2);
        assertThat(pagina.get("totalItens").asLong()).isEqualTo(4);
        assertThat(pagina.get("totalPaginas").asInt()).isEqualTo(2);
        assertThat(ultima.get("itens").size()).isEqualTo(2);
        assertThat(ultima.get("itens").get(1).get("nome").asText()).startsWith("Zelia");
        assertThat(busca.get("totalItens").asLong()).isEqualTo(2);
        assertThat(porNome.get("totalItens").asLong()).isEqualTo(1);
        assertThat(inativos.get("totalItens").asLong()).isEqualTo(1);
        assertThat(inativos.get("itens").get(0).get("id").asText()).isEqualTo(outroId);
        assertThat(grande.get("tamanho").asInt()).isEqualTo(100);
    }

    @Test
    void natyCadastraOPrimeiroAdminDeUmaEmpresa() {
        Conta naty = criarConta(criarEmpresa("Naty do Teste"), Papel.NATY);
        Empresa cliente = criarEmpresa("Cliente sem Admin");
        String email = emailNovo();

        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.POST,
                "/api/v1/painel/empresas/" + cliente.getId() + "/integrantes",
                novoIntegrante(email, Papel.ADMIN));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody().get("empresaId").asText())
                .isEqualTo(cliente.getId().toString());
        String tokenDoAdmin =
                autenticarCom(email, "senha-nova-123").getBody().get("token").asText();
        assertThat(chamar(tokenDoAdmin, HttpMethod.GET, INTEGRANTES).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void contaNatyNaoEhAdministradaPeloPainel() {
        Empresa interna = criarEmpresa("Naty Interna");
        Conta naty = criarConta(interna, Papel.NATY);
        Conta outroNaty = criarConta(interna, Papel.NATY);

        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.PUT,
                "/api/v1/painel/empresas/" + interna.getId() + "/integrantes/" + outroNaty.id() + "/ativo",
                Map.of("ativo", false));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CONTA_NATY_FORA_DO_PAINEL");
    }
}
