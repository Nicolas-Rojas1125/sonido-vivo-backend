package cl.sonidovivo.contactos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

// Verificación de los tokens JWT emitidos por ms-usuarios.
// Este microservicio NO emite tokens: solo comprueba la firma con la MISMA clave secreta compartida,
// sin necesidad de consultar a ms-usuarios en cada petición (arquitectura stateless).
@Configuration
public class JwtConfig {

    // Clave secreta a partir de jwt.secreto (application.properties o variable JWT_SECRETO).
    @Bean
    public SecretKey claveJwt(@Value("${jwt.secreto}") String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("jwt.secreto debe tener al menos 32 caracteres (256 bits) para HS256");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    // VERIFICA tokens en cada petición: firma, fecha de expiración (exp) y emisor (iss).
    @Bean
    public JwtDecoder jwtDecoder(SecretKey claveJwt, @Value("${jwt.emisor}") String emisor) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(claveJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(emisor));
        return decoder;
    }

    // Convierte el claim "rol" del token (ej: "ADMINISTRADOR") en el permiso ROLE_ADMINISTRADOR,
    // que es lo que revisan hasRole("ADMINISTRADOR") y @PreAuthorize.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }
}
