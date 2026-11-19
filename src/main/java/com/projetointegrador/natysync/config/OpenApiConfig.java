package com.projetointegrador.natysync.config;

import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BEARER = "sessao";

    static {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(IntegranteDaRequisicao.class);
    }

    @Bean
    public OpenApiCustomizer respostaDeNaoAutenticado() {
        return openApi -> {
            ModelConverters.getInstance().readAll(ErroResposta.class).forEach(openApi.getComponents()::addSchemas);
            openApi.getPaths().values().stream()
                    .flatMap(item -> item.readOperations().stream())
                    .filter(OpenApiConfig::exigeSessao)
                    .forEach(operacao -> operacao.getResponses().addApiResponse("401", naoAutenticado()));
        };
    }

    private static boolean exigeSessao(Operation operacao) {
        boolean semSeguranca =
                operacao.getSecurity() != null && operacao.getSecurity().isEmpty();
        return !semSeguranca && !operacao.getResponses().containsKey("401");
    }

    private static ApiResponse naoAutenticado() {
        Schema<?> erro = new Schema<>().$ref("#/components/schemas/ErroResposta");
        return new ApiResponse()
                .description("Token ausente, inválido ou expirado")
                .content(new Content().addMediaType("application/json", new MediaType().schema(erro)));
    }

    @Bean
    public OpenAPI documentacaoDaApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Naty Sync")
                        .version("v1")
                        .description("Trilha de treinamento das empresas clientes da Naty."
                                + " Toda rota exige Authorization: Bearer, exceto a emissão de sessão, o health e esta documentação."
                                + " As rotas de /api/v1/painel exigem papel ADMIN ou NATY."))
                .servers(List.of(new Server().url("/").description("Servidor que atende esta documentação")))
                .components(new Components()
                        .addSecuritySchemes(
                                ESQUEMA_BEARER,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .description("Token de POST /api/v1/sessoes, em Authorization: Bearer")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER));
    }
}
