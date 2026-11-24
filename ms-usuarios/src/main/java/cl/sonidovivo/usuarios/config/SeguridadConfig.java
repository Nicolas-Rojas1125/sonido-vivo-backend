package cl.sonidovivo.usuarios.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// Reglas de seguridad de ms-usuarios.
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http, JwtAuthenticationConverter convertidorJwt) throws Exception {
        http
                // API REST sin formularios ni sesiones del servidor: CSRF no aplica a peticiones con token Bearer.
                // (El refresh token va en una cookie SameSite=Strict, que el navegador no envía desde otros sitios.)
                .csrf(csrf -> csrf.disable())
                // Permite las peticiones del frontend (usa el bean corsConfigurationSource de abajo).
                .cors(Customizer.withDefaults())
                // STATELESS: el servidor no guarda sesiones; cada petición trae su token.
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Documentación Swagger y página de error: públicas.
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        // Login, registro, renovación y cierre de sesión: públicos (aún no hay token).
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro",
                                "/api/auth/refresh", "/api/auth/logout").permitAll()
                        // Gestión de usuarios: SOLO el Administrador.
                        .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                        // Todo lo demás (ej: /api/auth/yo) requiere un token válido.
                        .anyRequest().authenticated())
                // Valida el header "Authorization: Bearer <token>" con el JwtDecoder de JwtConfig.
                // Sin token o con token inválido/expirado -> 401. Con token pero sin el rol -> 403.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorJwt)));
        return http.build();
    }

    // BCrypt: encripta contraseñas con "sal" aleatoria; es lento a propósito para dificultar ataques de fuerza bruta.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS: el navegador solo deja que http://localhost:5173 (React) llame a esta API.
    // allowCredentials(true) es necesario para que viaje la cookie del refresh token.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes}") String[] origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(origenes));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }
}
