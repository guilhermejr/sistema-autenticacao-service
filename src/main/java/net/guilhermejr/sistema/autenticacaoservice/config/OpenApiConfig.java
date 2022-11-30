package net.guilhermejr.sistema.autenticacaoservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(
            @Value("${info.app.name}") String title,
            @Value("${info.app.description}") String description,
            @Value("${info.app.version}") String version
    ) {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name("Guilherme Jr.")
                                .email("falecom@guilhermejr.net")
                                .url("https://www.guilhermejr.net")));
    }

}
