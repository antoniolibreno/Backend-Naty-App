package com.projetointegrador.natysync.gamificacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

class CatalogoDeConquistasApiTest extends GamificacaoTest {

    @Test
    void catalogoInicialVemEmOrdemIgualParaTodasAsEmpresas() {
        Conta primeira = novaConta(criarEmpresa("Empresa catalogo A"), "Integrante A");
        Conta segunda = novaConta(criarEmpresa("Empresa catalogo B"), "Integrante B");

        assertThat(codigos(perfil(primeira)))
                .containsExactly(
                        "PRIMEIRA_ATIVIDADE",
                        "QUIZ_PERFEITO",
                        "MODULO_CONCLUIDO",
                        "TRILHA_CONCLUIDA",
                        "SEQUENCIA_3_DIAS",
                        "SEQUENCIA_7_DIAS",
                        "PONTOS_50");
        assertThat(perfil(segunda).get("conquistas")).isEqualTo(perfil(primeira).get("conquistas"));
    }

    private List<String> codigos(JsonNode perfil) {
        List<String> codigos = new ArrayList<>();
        perfil.get("conquistas")
                .forEach(conquista -> codigos.add(conquista.get("codigo").asText()));
        return codigos;
    }
}
