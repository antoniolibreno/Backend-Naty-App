package com.projetointegrador.natysync.acompanhamento;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.Usuario;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

abstract class BaseDeAcompanhamentoApiTest extends IntegracaoTest {

    protected static final List<UUID> ATIVIDADES = List.of(
            UUID.fromString("00000000-0000-0000-0002-000000000001"),
            UUID.fromString("00000000-0000-0000-0002-000000000002"),
            UUID.fromString("00000000-0000-0000-0002-000000000003"),
            UUID.fromString("00000000-0000-0000-0002-000000000004"),
            UUID.fromString("00000000-0000-0000-0002-000000000005"),
            UUID.fromString("00000000-0000-0000-0002-000000000006"));

    protected String tokenNaty;

    @BeforeEach
    void criarContaNaty() {
        Empresa interna = criarEmpresa("Empresa interna");
        String email = emailUnico("naty");
        criarIntegrante(interna, "Conta Naty", email, Papel.NATY);
        tokenNaty = autenticar(email, SENHA_DE_TESTE);
    }

    protected UUID integrante(Empresa empresa, String nome) {
        return criarIntegrante(empresa, nome, emailUnico("acomp"));
    }

    protected void criarEm(UUID usuarioId, OffsetDateTime momento) {
        jdbc.update("update usuario set criado_em = ? where id = ?", momento, usuarioId);
    }

    protected void concluir(UUID usuarioId, int quantas, OffsetDateTime ultimaEm) {
        for (int i = 0; i < quantas; i++) {
            jdbc.update(
                    "insert into progresso_atividade (usuario_id, atividade_id, concluido_em) values (?, ?, ?)",
                    usuarioId,
                    ATIVIDADES.get(i),
                    i == quantas - 1 ? ultimaEm : ultimaEm.minusHours(1));
        }
    }

    protected void desativar(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    protected ResponseEntity<JsonNode> ler(String uri) {
        return cliente(tokenNaty).get().uri(uri).retrieve().toEntity(JsonNode.class);
    }

    protected List<String> nomes(JsonNode pagina) {
        List<String> nomes = new ArrayList<>();
        pagina.get("itens").forEach(item -> nomes.add(item.get("nome").asText()));
        return nomes;
    }

    protected JsonNode item(JsonNode pagina, String nome) {
        for (JsonNode item : pagina.get("itens")) {
            if (item.get("nome").asText().equals(nome)) {
                return item;
            }
        }
        throw new AssertionError("Integrante nao encontrado na pagina: " + nome);
    }
}
