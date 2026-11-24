package cl.sonidovivo.usuarios.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

// Configuración de los tokens JWT (firmados con HMAC-SHA256 y una clave secreta compartida).
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

    // FIRMA tokens (solo ms-usuarios lo necesita, porque es el único que los emite).
    @Bean
    public JwtEncoder jwtEncoder(SecretKey claveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
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
