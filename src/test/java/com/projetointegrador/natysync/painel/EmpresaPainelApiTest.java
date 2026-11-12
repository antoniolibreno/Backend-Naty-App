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

class EmpresaPainelApiTest extends PainelTest {

    private Conta naty;

    @BeforeEach
    void criarNaty() {
        naty = criarConta(criarEmpresa("Naty de Teste"), Papel.NATY);
    }

    private ResponseEntity<JsonNode> criarPelaApi(String nome, String fuso) {
        ResponseEntity<JsonNode> resposta =
                chamar(naty.token(), HttpMethod.POST, "/api/v1/painel/empresas", CorpoDeEmpresa.de(nome, true, fuso));
        if (resposta.getStatusCode() == HttpStatus.CREATED) {
            registrarEmpresaCriada(UUID.fromString(resposta.getBody().get("id").asText()));
        }
        return resposta;
    }

    @Test
    void natyCriaELeEmpresaComFuso() {
        ResponseEntity<JsonNode> criada = criarPelaApi("Cliente Novo", "America/Manaus");

        assertThat(criada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode corpo = criada.getBody();
        assertThat(corpo.get("fusoHorario").asText()).isEqualTo("America/Manaus");
        assertThat(corpo.get("criadoEm").isNull()).isFalse();

        ResponseEntity<JsonNode> lida = chamar(
                naty.token(),
                HttpMethod.GET,
                "/api/v1/painel/empresas/" + corpo.get("id").asText());
        assertThat(lida.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lida.getBody().get("nome").asText()).isEqualTo("Cliente Novo");
    }

    @Test
    void fusoDesconhecidoEhErroDeValidacao() {
        ResponseEntity<JsonNode> resposta = criarPelaApi("Cliente Lua", "Lua/Crateras");

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
        assertThat(jdbc.queryForObject("select count(*) from empresa where nome = 'Cliente Lua'", Long.class))
                .isZero();
    }

    @Test
    void listagemEhPaginada() {
        criarPelaApi("Cliente A", "America/Sao_Paulo");
        criarPelaApi("Cliente B", "America/Sao_Paulo");

        JsonNode pagina = chamar(naty.token(), HttpMethod.GET, "/api/v1/painel/empresas?pagina=0&tamanho=1")
                .getBody();

        JsonNode segunda = chamar(naty.token(), HttpMethod.GET, "/api/v1/painel/empresas?pagina=1&tamanho=1")
                .getBody();

        assertThat(pagina.get("itens").size()).isEqualTo(1);
        assertThat(pagina.get("tamanho").asInt()).isEqualTo(1);
        assertThat(pagina.get("totalItens").asLong()).isGreaterThanOrEqualTo(3);
        assertThat(pagina.get("totalPaginas").asInt())
                .isEqualTo(pagina.get("totalItens").asInt());
        assertThat(segunda.get("pagina").asInt()).isEqualTo(1);
        assertThat(segunda.get("itens").get(0).get("id").asText())
                .isNotEqualTo(pagina.get("itens").get(0).get("id").asText());
    }

    @Test
    void excluirEmpresaComIntegranteEhConflito() {
        Empresa empresa = criarEmpresa("Com Integrante");
        criarIntegrante(empresa, "Alguem", "alguem-" + UUID.randomUUID() + "@teste.com.br");

        ResponseEntity<JsonNode> resposta =
                chamar(naty.token(), HttpMethod.DELETE, "/api/v1/painel/empresas/" + empresa.getId());

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("EMPRESA_COM_VINCULOS");
        assertThat(empresaRepository.existsById(empresa.getId())).isTrue();
    }

    @Test
    void excluirEmpresaSemIntegrante() {
        String id =
                criarPelaApi("Vazia", "America/Sao_Paulo").getBody().get("id").asText();

        assertThat(chamar(naty.token(), HttpMethod.DELETE, "/api/v1/painel/empresas/" + id)
                        .getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(empresaRepository.existsById(UUID.fromString(id))).isFalse();
    }

    @Test
    void desativarEmpresaDerrubaSessaoELoginDosIntegrantes() {
        Empresa empresa = criarEmpresa("Cliente que Sai");
        Conta integrante = criarConta(empresa, Papel.INTEGRANTE);
        assertThat(lerTrilhas(integrante.token()).getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.PUT,
                "/api/v1/painel/empresas/" + empresa.getId(),
                CorpoDeEmpresa.de("Cliente que Sai", false));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lerTrilhas(integrante.token()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(autenticarCom(integrante.email(), SENHA_DE_TESTE).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void empresaComContaNatyNaoEhDesativadaNemExcluida() {
        String caminho = "/api/v1/painel/empresas/" + naty.empresaId();

        ResponseEntity<JsonNode> desativar =
                chamar(naty.token(), HttpMethod.PUT, caminho, CorpoDeEmpresa.de("Naty de Teste", false));
        ResponseEntity<JsonNode> excluir = chamar(naty.token(), HttpMethod.DELETE, caminho);

        assertThat(desativar.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(desativar.getBody().get("codigo").asText()).isEqualTo("EMPRESA_COM_CONTA_NATY");
        assertThat(excluir.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(excluir.getBody().get("codigo").asText()).isEqualTo("EMPRESA_COM_CONTA_NATY");
        assertThat(lerTrilhas(naty.token()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void natyNaoTrancaOutroNatyPelaEmpresa() {
        Empresa outraInterna = criarEmpresa("Outra Naty");
        Conta outroNaty = criarConta(outraInterna, Papel.NATY);

        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.PUT,
                "/api/v1/painel/empresas/" + outraInterna.getId(),
                CorpoDeEmpresa.de("Outra Naty", false));

        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("EMPRESA_COM_CONTA_NATY");
        assertThat(lerTrilhas(outroNaty.token()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void reativarEmpresaNaoRessuscitaTokenAntigo() {
        Empresa empresa = criarEmpresa("Cliente que Volta");
        Conta integrante = criarConta(empresa, Papel.INTEGRANTE);
        String caminho = "/api/v1/painel/empresas/" + empresa.getId();

        chamar(naty.token(), HttpMethod.PUT, caminho, CorpoDeEmpresa.de("Cliente que Volta", false));
        chamar(naty.token(), HttpMethod.PUT, caminho, CorpoDeEmpresa.de("Cliente que Volta", true));

        assertThat(lerTrilhas(integrante.token()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(autenticarCom(integrante.email(), SENHA_DE_TESTE).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void empresaInexistenteEh404() {
        assertThat(chamar(naty.token(), HttpMethod.GET, "/api/v1/painel/empresas/" + UUID.randomUUID())
                        .getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<JsonNode> criar(Map<String, Object> corpo) {
        ResponseEntity<JsonNode> resposta = chamar(naty.token(), HttpMethod.POST, "/api/v1/painel/empresas", corpo);
        if (resposta.getStatusCode() == HttpStatus.CREATED) {
            registrarEmpresaCriada(UUID.fromString(resposta.getBody().get("id").asText()));
        }
        return resposta;
    }

    private long totalDeEmpresas() {
        return jdbc.queryForObject("select count(*) from empresa", Long.class);
    }

    @Test
    void criacaoCompletaDevolveCnpjERazaoSocial() {
        String cnpj = CorpoDeEmpresa.cnpjNovo();

        ResponseEntity<JsonNode> resposta = criar(CorpoDeEmpresa.comCnpj("Cliente Completo", cnpj));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody().get("cnpj").asText()).isEqualTo(cnpj);
        assertThat(resposta.getBody().get("razaoSocial").asText()).isEqualTo("Cliente Completo Ltda");
    }

    @Test
    void criacaoSemCnpjOuSemRazaoSocialEhErroDeValidacao() {
        long antes = totalDeEmpresas();

        ResponseEntity<JsonNode> semCnpj = criar(CorpoDeEmpresa.sem("Sem Cnpj", "cnpj"));
        ResponseEntity<JsonNode> semRazao = criar(CorpoDeEmpresa.sem("Sem Razao", "razaoSocial"));

        assertThat(semCnpj.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(semCnpj.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
        assertThat(semRazao.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(semRazao.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
        assertThat(totalDeEmpresas()).isEqualTo(antes);
    }

    @Test
    void alteracaoSemCnpjEhErroDeValidacao() {
        Empresa empresa = criarEmpresa("Para Alterar");

        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.PUT,
                "/api/v1/painel/empresas/" + empresa.getId(),
                CorpoDeEmpresa.sem("Para Alterar", "cnpj"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void empresaGravadaSemDadosCadastraisEhLidaComCamposNulos() {
        Empresa empresa = criarEmpresa("Sem Dados Cadastrais");

        JsonNode corpo = chamar(naty.token(), HttpMethod.GET, "/api/v1/painel/empresas/" + empresa.getId())
                .getBody();

        assertThat(corpo.get("cnpj").isNull()).isTrue();
        assertThat(corpo.get("razaoSocial").isNull()).isTrue();
    }

    @Test
    void cnpjComMascaraEhGuardadoSoComOsCatorzeCaracteres() {
        String cnpj = CorpoDeEmpresa.cnpjNovo();

        ResponseEntity<JsonNode> resposta =
                criar(CorpoDeEmpresa.comCnpj("Cliente Mascara", CorpoDeEmpresa.comMascara(cnpj)));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody().get("cnpj").asText()).isEqualTo(cnpj);
        assertThat(jdbc.queryForObject(
                        "select cnpj from empresa where id = ?",
                        String.class,
                        UUID.fromString(resposta.getBody().get("id").asText())))
                .isEqualTo(cnpj);
    }

    @Test
    void cnpjAlfanumericoValidoEhAceitoEMaiusculizado() {
        ResponseEntity<JsonNode> resposta = criar(CorpoDeEmpresa.comCnpj("Cliente Alfa", "12.abc.345/01de-35"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody().get("cnpj").asText()).isEqualTo("12ABC34501DE35");
    }

    @Test
    void cnpjComDigitoErradoOuRepetidoEhRecusado() {
        long antes = totalDeEmpresas();

        ResponseEntity<JsonNode> digitoErrado = criar(CorpoDeEmpresa.comCnpj("Cliente Errado", "11.222.333/0001-82"));
        ResponseEntity<JsonNode> repetido = criar(CorpoDeEmpresa.comCnpj("Cliente Zeros", "00000000000000"));

        assertThat(digitoErrado.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(digitoErrado.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
        assertThat(repetido.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(totalDeEmpresas()).isEqualTo(antes);
    }

    @Test
    void criarComCnpjJaCadastradoEhConflitoComOuSemMascara() {
        String cnpj = CorpoDeEmpresa.cnpjNovo();
        criar(CorpoDeEmpresa.comCnpj("Primeira", cnpj));
        long antes = totalDeEmpresas();

        ResponseEntity<JsonNode> igual = criar(CorpoDeEmpresa.comCnpj("Segunda", cnpj));
        ResponseEntity<JsonNode> mascarado = criar(CorpoDeEmpresa.comCnpj("Terceira", CorpoDeEmpresa.comMascara(cnpj)));

        assertThat(igual.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(igual.getBody().get("codigo").asText()).isEqualTo("CNPJ_JA_CADASTRADO");
        assertThat(mascarado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(totalDeEmpresas()).isEqualTo(antes);
    }

    @Test
    void alterarParaCnpjDeOutraEmpresaEhConflito() {
        String cnpjDaOutra = CorpoDeEmpresa.cnpjNovo();
        criar(CorpoDeEmpresa.comCnpj("Dona do Cnpj", cnpjDaOutra));
        String id = criar(CorpoDeEmpresa.de("Quer Roubar")).getBody().get("id").asText();

        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.PUT,
                "/api/v1/painel/empresas/" + id,
                CorpoDeEmpresa.comCnpj("Quer Roubar", cnpjDaOutra));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CNPJ_JA_CADASTRADO");
    }

    @Test
    void alterarOutroDadoReenviandoOProprioCnpjNaoEhConflito() {
        String cnpj = CorpoDeEmpresa.cnpjNovo();
        String id = criar(CorpoDeEmpresa.comCnpj("Nome Antigo", cnpj))
                .getBody()
                .get("id")
                .asText();

        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.PUT,
                "/api/v1/painel/empresas/" + id,
                CorpoDeEmpresa.comCnpj("Nome Novo", cnpj));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("nome").asText()).isEqualTo("Nome Novo");
        assertThat(resposta.getBody().get("cnpj").asText()).isEqualTo(cnpj);
    }

    private static final String EMPRESAS = "/api/v1/painel/empresas";

    private String marca() {
        return "Marca" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private JsonNode buscar(String parametros) {
        return chamar(naty.token(), HttpMethod.GET, EMPRESAS + "?" + parametros).getBody();
    }

    private java.util.List<String> nomes(JsonNode pagina) {
        java.util.List<String> nomes = new java.util.ArrayList<>();
        pagina.get("itens").forEach(item -> nomes.add(item.get("nome").asText()));
        return nomes;
    }

    @Test
    void buscaPorParteDoNomeIgnoraCaixa() {
        String marca = marca();
        criar(CorpoDeEmpresa.de(marca + " Treinamentos"));
        criar(CorpoDeEmpresa.de("Outra Empresa"));

        JsonNode pagina = buscar("busca=" + marca.toLowerCase());

        assertThat(nomes(pagina)).containsExactly(marca + " Treinamentos");
    }

    @Test
    void buscaPorRazaoSocialEmMaiusculasSemCasarComONome() {
        String marca = marca();
        criar(CorpoDeEmpresa.comRazaoSocial("Nome Sem Marca " + marca.length(), marca + "Razao"));

        JsonNode pagina = buscar("busca=" + marca.toUpperCase() + "RAZAO");

        assertThat(nomes(pagina)).containsExactly("Nome Sem Marca " + marca.length());
    }

    @Test
    void buscaPorCnpjParcialComEmMascaraOuSem() {
        String cnpj = CorpoDeEmpresa.cnpjNovo();
        String nome = marca();
        criar(CorpoDeEmpresa.comCnpj(nome, cnpj));
        String comMascara = CorpoDeEmpresa.comMascara(cnpj);

        assertThat(nomes(buscar("busca=" + cnpj))).containsExactly(nome);
        assertThat(nomes(buscar("busca=" + comMascara))).containsExactly(nome);
        assertThat(nomes(buscar("busca=" + comMascara.substring(0, 10)))).contains(nome);
    }

    @Test
    void buscaSemResultadoDevolvePaginaVazia() {
        JsonNode pagina = buscar("busca=" + marca() + "inexistente");

        assertThat(pagina.get("itens").size()).isZero();
        assertThat(pagina.get("totalItens").asLong()).isZero();
        assertThat(pagina.get("totalPaginas").asInt()).isZero();
    }

    @Test
    void curingasDaBuscaSaoLiteraisENaoDevolvemTodasAsEmpresas() {
        criar(CorpoDeEmpresa.de(marca()));

        assertThat(buscar("busca=%").get("itens").size()).isZero();
        assertThat(buscar("busca=_").get("itens").size()).isZero();
    }

    @Test
    void buscaVaziaEquivaleAListagemSemBusca() {
        criar(CorpoDeEmpresa.de(marca()));

        JsonNode semBusca = buscar("tamanho=100");
        JsonNode comBuscaVazia = buscar("busca=&tamanho=100");

        assertThat(comBuscaVazia.get("totalItens").asLong())
                .isEqualTo(semBusca.get("totalItens").asLong());
        assertThat(nomes(comBuscaVazia)).isEqualTo(nomes(semBusca));
    }

    @Test
    void ordemPorNomeEMantidaComBusca() {
        String marca = marca();
        criar(CorpoDeEmpresa.de(marca + " B"));
        criar(CorpoDeEmpresa.de(marca + " A"));
        criar(CorpoDeEmpresa.de(marca + " C"));

        assertThat(nomes(buscar("busca=" + marca))).containsExactly(marca + " A", marca + " B", marca + " C");
    }

    @Test
    void totalDePaginasConsideraSoOsResultadosDaBusca() {
        String marca = marca();
        criar(CorpoDeEmpresa.de(marca + " 1"));
        criar(CorpoDeEmpresa.de(marca + " 2"));
        criar(CorpoDeEmpresa.de(marca + " 3"));

        JsonNode primeira = buscar("busca=" + marca + "&tamanho=2&pagina=0");
        JsonNode segunda = buscar("busca=" + marca + "&tamanho=2&pagina=1");

        assertThat(primeira.get("totalItens").asLong()).isEqualTo(3);
        assertThat(primeira.get("totalPaginas").asInt()).isEqualTo(2);
        assertThat(primeira.get("itens").size()).isEqualTo(2);
        assertThat(segunda.get("itens").size()).isEqualTo(1);
    }
}
