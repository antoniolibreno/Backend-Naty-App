package com.projetointegrador.natysync.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.JsonNode;

@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = {"logging.level.root=DEBUG", "logging.level.org.springframework.web.client=INFO"})
class LogSemCredencialTest extends IntegracaoTest {

    private final String email = emailUnico("log");

    private JsonNode entrar(String senha) {
        return clienteSemSessao()
                .post()
                .uri("/api/v1/sessoes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "senha", senha))
                .retrieve()
                .body(JsonNode.class);
    }

    @Test
    void senhaErradaNaoApareceNoLog(CapturedOutput saida) {
        Empresa empresa = criarEmpresa("Empresa Log");
        criarIntegrante(empresa, "Integrante Log", email);
        String senhaErrada = "errada-" + UUID.randomUUID();

        entrar(senhaErrada);

        assertThat(saida.getAll()).isNotBlank().doesNotContain(senhaErrada);
    }

    @Test
    void senhaETokenDoLoginValidoNaoAparecemNoLog(CapturedOutput saida) {
        Empresa empresa = criarEmpresa("Empresa Log");
        criarIntegrante(empresa, "Integrante Log", email);

        String hash = usuarioRepository.findAll().stream()
                .filter(usuario -> email.equals(usuario.getEmail()))
                .findFirst()
                .orElseThrow()
                .getSenhaHash();

        String token = entrar(SENHA_DE_TESTE).get("token").asText();

        assertThat(saida.getAll())
                .isNotBlank()
                .doesNotContain(SENHA_DE_TESTE)
                .doesNotContain(hash)
                .doesNotContain(token);
    }

    @Test
    void tokenNaChamadaAutenticadaNaoApareceNoLog(CapturedOutput saida) {
        Empresa empresa = criarEmpresa("Empresa Log");
        criarIntegrante(empresa, "Integrante Log", email);
        String token = autenticar(email, SENHA_DE_TESTE);

        cliente(token).get().uri("/api/v1/usuarios").retrieve().toBodilessEntity();
        cliente(token).get().uri("/api/v1/rota-inexistente").retrieve().toBodilessEntity();

        assertThat(saida.getAll()).isNotBlank().doesNotContain(token);
    }
}
