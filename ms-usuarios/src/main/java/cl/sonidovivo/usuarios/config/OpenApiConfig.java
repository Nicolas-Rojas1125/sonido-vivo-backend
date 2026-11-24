package cl.sonidovivo.usuarios.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger (OpenAPI): documentación en http://localhost:8081/swagger-ui.html
// Agrega el botón "Authorize" para pegar el token JWT y probar los endpoints protegidos.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentacion() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sonido Vivo - ms-usuarios")
                        .version("1.0")
                        .description("Autenticación con JWT (login, registro, renovación y cierre de sesión) "
                                + "y gestión de usuarios y roles. Para probar los endpoints protegidos: "
                                + "ejecuta POST /api/auth/login, copia el accessToken y pégalo en Authorize."))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
