package br.gov.caixa.caixaverso.investimento.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Define o esquema de segurança (Security Scheme) para Bearer Token (JWT).
     * Isso cria a definição "bearerAuth" que podemos referenciar.
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    /**
     * Configuração central da documentação OpenAPI (Swagger).
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // 1. Adiciona a definição de segurança "bearerAuth"
                .components(new Components().addSecuritySchemes("bearerAuth", createAPIKeyScheme()))

                // 2. Define que a segurança "bearerAuth" é um requisito global
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))

                // 3. Define as informações gerais da API
                .info(new Info().title("API de Investimentos - CaixaVerso")
                        .description("API para simulação e recomendação de produtos de investimento.")
                        .version("1.0.0")
                        .contact(new Contact().name("Time CaixaVerso Java").email("reinaldo.simoes@caixa.gov.br"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}