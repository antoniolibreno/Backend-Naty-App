package com.projetointegrador.natysync.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class ConfiguracaoDeProducaoTest {

    private Object propriedade(String nome) throws IOException {
        PropertySource<?> fonte = new YamlPropertySourceLoader()
                .load("producao", new ClassPathResource("application-prod.yml"))
                .getFirst();
        return fonte.getProperty(nome);
    }

    @Test
    void retencaoDeSessaoNaoTemValorPadraoEmProducao() throws IOException {
        assertThat(propriedade("app.sessao.retencao")).isEqualTo("${APP_SESSAO_RETENCAO}");
    }

    @Test
    void expiracaoDeSessaoNaoTemValorPadraoEmProducao() throws IOException {
        assertThat(propriedade("app.sessao.expiracao")).isEqualTo("${APP_SESSAO_EXPIRACAO}");
    }

    @Test
    void duracaoMaximaDeSessaoNaoTemValorPadraoEmProducao() throws IOException {
        assertThat(propriedade("app.sessao.duracao-maxima")).isEqualTo("${APP_SESSAO_DURACAO_MAXIMA}");
    }
}
