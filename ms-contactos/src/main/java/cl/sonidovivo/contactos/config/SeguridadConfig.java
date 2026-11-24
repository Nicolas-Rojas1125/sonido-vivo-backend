package cl.sonidovivo.contactos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// Reglas de acceso por rol de ms-contactos. Se revisan EN ORDEN: la primera que coincide decide.
// Un INVITADO (sin token) puede enviar un mensaje desde el formulario de contacto;
// leer, atender y eliminar mensajes sigue siendo solo para el personal de la tienda.
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String VENDEDOR = "VENDEDOR";

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http, JwtAuthenticationConverter convertidorJwt) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()

                        // Enviar un mensaje: público (invitados incluidos). Los datos se validan con @Valid.
                        .requestMatchers(HttpMethod.POST, "/api/contactos").permitAll()
                        // Leer, marcar como atendido y eliminar: Administrador y Vendedor.
                        .requestMatchers("/api/contactos", "/api/contactos/**").hasAnyRole(ADMIN, VENDEDOR)

                        // Cualquier otra ruta: denegada.
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorJwt)));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes}") String[] origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(origenes));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }
}
