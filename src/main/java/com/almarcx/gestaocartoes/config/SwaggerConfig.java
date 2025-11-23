package com.empresa.gestao_cartoes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//SwaggerConfig.java (Configuração de Documentação API)

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Gestão de Cartões")
                        .version("1.0")
                        .description("API para gestão de cartões de crédito e clientes"));
    }
}