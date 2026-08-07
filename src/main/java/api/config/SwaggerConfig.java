package api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("API Bank — Gerenciamento Financeiro")
                                                .version("1.0.0")
                                                .description("API REST com autenticação JWT")
                                                .contact(new Contact()
                                                                .name("Amadeus Bertoline")
                                                                .email("amadeusbertoline123@gmail.com")))
                                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                                .servers(List.of(new Server().url("/")))
                                .components(new Components()
                                                .addSecuritySchemes("Bearer", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")));
        }

        @Bean
        public OpenApiCustomizer sortTagsCustom() {
                return openApi -> {
                        // Define a ordem exata das tags
                        List<String> ordemDesejada = List.of(
                                        "Autenticação",
                                        "Contas",
                                        "Usuários",
                                        "Chaves Pix",
                                        "Transações",
                                        "Admin");

                        if (openApi.getTags() != null) {
                                openApi.getTags().sort(Comparator.comparingInt(tag -> {
                                        int index = ordemDesejada.indexOf(tag.getName());
                                        // Tags não mapeadas na lista vão para o final
                                        return index != -1 ? index : Integer.MAX_VALUE;
                                }));
                        }
                };
        }
}