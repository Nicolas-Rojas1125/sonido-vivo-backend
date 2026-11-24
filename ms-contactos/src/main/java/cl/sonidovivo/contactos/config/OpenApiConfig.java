package cl.sonidovivo.contactos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger (OpenAPI): documentación en http://localhost:8082/swagger-ui.html
// Agrega el botón "Authorize" para pegar el token JWT y probar los endpoints protegidos.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentacion() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sonido Vivo - ms-contactos")
                        .version("1.0")
                        .description("Mensajes del formulario de contacto. Todos los endpoints requieren un token JWT: " + "obtenlo con POST /api/auth/login en ms-usuarios (http://localhost:8081/swagger-ui.html) y pégalo en Authorize."))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
