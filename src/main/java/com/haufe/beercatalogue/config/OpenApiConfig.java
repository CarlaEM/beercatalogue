package com.haufe.beercatalogue.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI beerCatalogueOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                    .title("Beer Catalogue API")
                    .description("API documentation for Beer Catalogue app")
                    .version("v1.0"));
    }
}
