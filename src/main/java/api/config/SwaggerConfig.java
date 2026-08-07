package api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

                .tags(List.of(
                        new Tag().name("Autenticação").description("Endpoints de login e geração de token"),
                        new Tag().name("Contas").description("Operações e consultas de contas bancárias"),
                        new Tag().name("Usuários").description("Gerenciamento e visualização de dados do usuário"),
                        new Tag().name("Chaves Pix").description("Gestão de chaves Pix"),
                        new Tag().name("Transações").description("Realizar transação e extrato"),
                        new Tag().name("Admin").description("Cadastro de administradores, gerenciamento de contas e visualização de dados de usuários")
                ))

                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .servers(List.of(new Server().url("/")))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}