package com.projetointegrador.natysync;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestcontainerConfiguration.class)
public abstract class IntegracaoTest {

    protected static final String SENHA_DE_TESTE = "senha-de-teste";

    @LocalServerPort
    protected int porta;

    @Autowired
    protected EmpresaRepository empresaRepository;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected PasswordEncoder codificadorDeSenha;

    private UUID empresaPadraoId;
    private UUID integrantePadraoId;
    private String tokenPadrao;

    @AfterEach
    void removerIntegrantePadrao() {
        if (integrantePadraoId != null) {
            usuarioRepository.deleteById(integrantePadraoId);
            empresaRepository.deleteById(empresaPadraoId);
            integrantePadraoId = null;
            empresaPadraoId = null;
            tokenPadrao = null;
        }
    }

    protected RestClient clienteSemSessao() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> {})
                .build();
    }

    protected RestClient cliente(String token) {
        return RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultStatusHandler(status -> true, (requisicao, resposta) -> {})
                .build();
    }

    protected RestClient cliente() {
        return cliente(tokenPadrao());
    }

    protected String tokenPadrao() {
        if (tokenPadrao == null) {
            Empresa empresa = criarEmpresa("Empresa Padrao de Teste");
            empresaPadraoId = empresa.getId();
            String email = "padrao-" + UUID.randomUUID() + "@teste.com.br";
            integrantePadraoId = criarIntegrante(empresa, "Integrante Padrao", email);
            tokenPadrao = autenticar(email, SENHA_DE_TESTE);
        }
        return tokenPadrao;
    }

    protected Empresa criarEmpresa(String nome) {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setNome(nome);
        empresa.setAtiva(true);
        empresa.setCriadoEm(OffsetDateTime.now());
        empresa.setAtualizadoEm(OffsetDateTime.now());
        return empresaRepository.save(empresa);
    }

    protected UUID criarIntegrante(Empresa empresa, String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setEmpresa(empresa);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPerfil("user");
        usuario.setStatus("offline");
        usuario.setSenhaHash(codificadorDeSenha.encode(SENHA_DE_TESTE));
        return usuarioRepository.save(usuario).getId();
    }

    protected String autenticar(String email, String senha) {
        JsonNode corpo = clienteSemSessao()
                .post()
                .uri("/api/v1/sessoes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "senha", senha))
                .retrieve()
                .body(JsonNode.class);
        return corpo.get("token").asText();
    }
}
