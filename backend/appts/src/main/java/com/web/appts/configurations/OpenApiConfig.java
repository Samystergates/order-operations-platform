package com.web.appts.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Platform API")
                        .description("Internal order operations, workflow, transport, archive, and auth API.")
                        .version("v1")
                        .contact(new Contact().name("Order Platform Team"))
                        .license(new License().name("Private")));
    }
}
