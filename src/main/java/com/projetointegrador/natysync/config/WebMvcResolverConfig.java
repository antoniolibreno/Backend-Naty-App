package com.projetointegrador.natysync.config;

import com.projetointegrador.natysync.usuario.IntegranteArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcResolverConfig implements WebMvcConfigurer {

    private final IntegranteArgumentResolver integranteArgumentResolver;

    public WebMvcResolverConfig(IntegranteArgumentResolver integranteArgumentResolver) {
        this.integranteArgumentResolver = integranteArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvedores) {
        resolvedores.add(integranteArgumentResolver);
    }
}
