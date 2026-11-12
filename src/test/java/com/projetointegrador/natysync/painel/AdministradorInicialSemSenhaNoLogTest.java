package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.usuario.Papel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.JsonNode;

@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = {"logging.level.root=DEBUG", "logging.level.org.springframework.web.client=INFO"})
class AdministradorInicialSemSenhaNoLogTest extends PainelTest {

    private static final String SENHA_DO_ADMINISTRADOR = "senha-secreta-do-admin-9X";

    @Test
    void senhaDoAdministradorNaoApareceNoLogNemNaCriacaoNemNaFalha(CapturedOutput saida) {
        Conta naty = criarConta(criarEmpresa("Naty do Log"), Papel.NATY);
        Map<String, Object> administrador = CorpoDeEmpresa.administradorCom("senha", SENHA_DO_ADMINISTRADOR);
        Map<String, Object> telefoneInvalido = new HashMap<>(administrador);
        telefoneInvalido.put("telefone", "123");

        ResponseEntity<JsonNode> criada = chamar(
                naty.token(),
                HttpMethod.POST,
                "/api/v1/painel/empresas",
                CorpoDeEmpresa.comAdministrador("Log Valido", administrador));
        registrarEmpresaCriada(UUID.fromString(criada.getBody().get("id").asText()));
        ResponseEntity<JsonNode> repetido = chamar(
                naty.token(),
                HttpMethod.POST,
                "/api/v1/painel/empresas",
                CorpoDeEmpresa.comAdministrador("Log Repetido", administrador));
        ResponseEntity<JsonNode> invalido = chamar(
                naty.token(),
                HttpMethod.POST,
                "/api/v1/painel/empresas",
                CorpoDeEmpresa.comAdministrador("Log Invalido", telefoneInvalido));

        assertThat(criada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(repetido.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(invalido.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(saida.getAll()).isNotBlank().doesNotContain(SENHA_DO_ADMINISTRADOR);
    }
}
