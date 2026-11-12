package com.projetointegrador.natysync.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import tools.jackson.databind.JsonNode;

class DocumentacaoOpenApiApiTest extends IntegracaoTest {

    private static final Set<String> METODOS = Set.of("get", "post", "put", "patch", "delete");
    private static final Set<String> TIPOS_ESCALARES = Set.of("string", "integer", "number", "boolean");

    private JsonNode documento;

    @BeforeEach
    void lerDocumento() {
        documento = clienteSemSessao().get().uri("/v3/api-docs").retrieve().body(JsonNode.class);
    }

    @Test
    void todoSchemaDeRequisicaoERespostaTemDescricaoEExemploNosCampos() {
        List<String> falhas = new ArrayList<>();
        documento.at("/components/schemas").properties().forEach(schema -> {
            String nome = schema.getKey();
            if (!nome.endsWith("Request") && !nome.endsWith("Response")) {
                return;
            }
            if (!schema.getValue().hasNonNull("description")) {
                falhas.add(nome + " sem description");
            }
            schema.getValue().path("properties").properties().forEach(campo -> {
                JsonNode definicao = campo.getValue();
                if (definicao.has("$ref")) {
                    return;
                }
                if (!definicao.hasNonNull("description")) {
                    falhas.add(nome + "." + campo.getKey() + " sem description");
                }
                if (TIPOS_ESCALARES.contains(definicao.path("type").asString("")) && !definicao.has("example")) {
                    falhas.add(nome + "." + campo.getKey() + " sem example");
                }
            });
        });
        assertThat(falhas).isEmpty();
    }

    @Test
    void todaOperacaoTemResumoDescricaoEOCorpoDeErroDocumentado() {
        List<String> falhas = new ArrayList<>();
        documento.at("/paths").properties().forEach(caminho -> caminho.getValue().properties().stream()
                .filter(metodo -> METODOS.contains(metodo.getKey()))
                .forEach(metodo -> {
                    String rotulo = metodo.getKey().toUpperCase() + " " + caminho.getKey();
                    JsonNode operacao = metodo.getValue();
                    if (!operacao.hasNonNull("summary")) {
                        falhas.add(rotulo + " sem summary");
                    }
                    JsonNode respostas = operacao.path("responses");
                    boolean publica = operacao.has("security")
                            && operacao.path("security").isEmpty();
                    if (!publica && !respostas.has("401")) {
                        falhas.add(rotulo + " sem resposta 401");
                    }
                    respostas.properties().forEach(resposta -> {
                        if (resposta.getKey().compareTo("400") >= 0
                                && !resposta.getValue().has("content")) {
                            falhas.add(rotulo + " resposta " + resposta.getKey() + " sem corpo de erro");
                        }
                    });
                }));
        assertThat(falhas).isEmpty();
    }

    @Test
    void camposObrigatoriosDoSchemaBatemComABeanValidation() {
        Map<String, List<Class<?>>> recordsPorNome = recordsDeRequisicaoEResposta();
        List<String> falhas = new ArrayList<>();
        documento.at("/components/schemas").properties().forEach(schema -> {
            List<Class<?>> candidatos = recordsPorNome.getOrDefault(schema.getKey(), List.of());
            if (candidatos.size() != 1) {
                return;
            }
            Set<String> obrigatorios = new HashSet<>();
            schema.getValue().path("required").forEach(campo -> obrigatorios.add(campo.asString()));
            for (RecordComponent componente : candidatos.get(0).getRecordComponents()) {
                boolean validado = componente.isAnnotationPresent(NotNull.class)
                        || componente.isAnnotationPresent(NotBlank.class)
                        || componente.isAnnotationPresent(NotEmpty.class);
                if (validado && !obrigatorios.contains(componente.getName())) {
                    falhas.add(schema.getKey() + "." + componente.getName() + " validado e fora de required");
                }
            }
        });
        assertThat(falhas).isEmpty();
    }

    private Map<String, List<Class<?>>> recordsDeRequisicaoEResposta() {
        var varredor = new ClassPathScanningCandidateComponentProvider(false);
        varredor.addIncludeFilter(new RegexPatternTypeFilter(java.util.regex.Pattern.compile(".*(Request|Response)")));
        Set<BeanDefinition> definicoes = varredor.findCandidateComponents("com.projetointegrador.natysync");
        return definicoes.stream()
                .map(definicao -> carregar(definicao.getBeanClassName()))
                .filter(Class::isRecord)
                .collect(Collectors.groupingBy(Class::getSimpleName));
    }

    private static Class<?> carregar(String nome) {
        try {
            return Class.forName(nome);
        } catch (ClassNotFoundException excecao) {
            throw new IllegalStateException(excecao);
        }
    }
}
