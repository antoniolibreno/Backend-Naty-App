package com.projetointegrador.natysync.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BEARER = "sessao";

    @Bean
    public OpenAPI documentacaoDaApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Naty Sync")
                        .version("v1")
                        .description("Trilha de treinamento dos integrantes das empresas clientes da Naty."
                                + " Toda rota exige Authorization: Bearer, com excecao da emissao de sessao,"
                                + " da verificacao de saude e desta documentacao."))
                .servers(List.of(new Server().url("/").description("Servidor que atende esta documentacao")))
                .components(new Components()
                        .addSecuritySchemes(
                                ESQUEMA_BEARER,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .description("Token opaco devolvido por POST /api/v1/sessoes")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER));
    }
}
