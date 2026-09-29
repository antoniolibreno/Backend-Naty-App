package com.projetointegrador.natysync;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    protected JdbcTemplate jdbc;

    private final List<UUID> empresasCriadas = new ArrayList<>();
    private String tokenPadrao;

    @AfterEach
    void removerEmpresasCriadas() {
        for (UUID empresaId : empresasCriadas) {
            jdbc.update("delete from usuario where empresa_id = ?", empresaId);
            jdbc.update("delete from empresa where id = ?", empresaId);
        }
        empresasCriadas.clear();
        tokenPadrao = null;
    }

    protected void registrarEmpresaCriada(UUID empresaId) {
        empresasCriadas.add(empresaId);
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
            String email = "padrao-" + UUID.randomUUID() + "@teste.com.br";
            criarIntegrante(empresa, "Integrante Padrao", email);
            tokenPadrao = autenticar(email, SENHA_DE_TESTE);
        }
        return tokenPadrao;
    }

    protected Empresa criarEmpresa(String nome) {
        Empresa empresa = new Empresa();
        empresa.setNome(nome);
        Empresa salva = empresaRepository.save(empresa);
        registrarEmpresaCriada(salva.getId());
        return salva;
    }

    protected UUID criarIntegrante(Empresa empresa, String nome, String email) {
        return criarIntegrante(empresa, nome, email, Papel.INTEGRANTE);
    }

    protected UUID criarIntegrante(Empresa empresa, String nome, String email, Papel papel) {
        Usuario usuario = new Usuario();
        usuario.setPapel(papel);
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
