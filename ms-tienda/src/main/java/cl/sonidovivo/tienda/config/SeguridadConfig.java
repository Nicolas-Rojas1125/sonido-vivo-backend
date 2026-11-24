package cl.sonidovivo.tienda.config;

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

// Reglas de acceso por rol de ms-tienda. Se revisan EN ORDEN: la primera que coincide decide.
// Tienda abierta: un INVITADO (sin token) puede ver el catálogo, igual que en una tienda online convencional.
// Comprar y ver pedidos exige cuenta de CLIENTE; gestionar productos, stock y pedidos exige rol del personal.
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String VENDEDOR = "VENDEDOR";
    private static final String CLIENTE = "CLIENTE";

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http, JwtAuthenticationConverter convertidorJwt) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()

                        // Catálogo: público (invitados incluidos). Es solo lectura y no expone datos personales.
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/*", "/api/categorias").permitAll()
                        // Inventario: Administrador y Vendedor pueden ajustar el stock.
                        .requestMatchers(HttpMethod.PATCH, "/api/productos/*/stock").hasAnyRole(ADMIN, VENDEDOR)
                        // Crear, editar y eliminar productos: solo Administrador.
                        .requestMatchers("/api/productos/**").hasRole(ADMIN)

                        // Pedidos
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasRole(CLIENTE)             // comprar
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/mios").hasRole(CLIENTE)         // mis pedidos
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/resumen").hasAnyRole(ADMIN, VENDEDOR)
                        .requestMatchers(HttpMethod.GET, "/api/pedidos").hasAnyRole(ADMIN, VENDEDOR)    // todos
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/estado").hasAnyRole(ADMIN, VENDEDOR)
                        // Ver un pedido: el servicio comprueba que sea del cliente o que sea personal de la tienda.
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/*").authenticated()

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
