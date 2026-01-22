package br.com.easybiz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI easyBizOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("EasyBiz API")
                .description("Plataforma de serviços flexíveis — conectando clientes e prestadores")
                .version("1.0.0")
            );
    }
}

