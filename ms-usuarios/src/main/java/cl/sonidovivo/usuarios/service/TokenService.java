package cl.sonidovivo.usuarios.service;

import cl.sonidovivo.usuarios.config.ErrorNegocio;
import cl.sonidovivo.usuarios.model.RefreshToken;
import cl.sonidovivo.usuarios.model.Usuario;
import cl.sonidovivo.usuarios.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

// Genera, verifica y renueva los tokens.
//  - Access token (JWT, 15 min): se envía en cada petición como "Authorization: Bearer ...".
//  - Refresh token (aleatorio, 7 días): solo sirve para pedir un nuevo access token. Se ROTA en cada uso.
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokens;
    private final String emisor;
    private final Duration duracionAcceso;
    private final Duration duracionRefresco;
    private final SecureRandom aleatorio = new SecureRandom();

    public TokenService(JwtEncoder jwtEncoder,
                        RefreshTokenRepository refreshTokens,
                        @Value("${jwt.emisor}") String emisor,
                        @Value("${jwt.minutos-acceso}") long minutosAcceso,
                        @Value("${jwt.dias-refresco}") long diasRefresco) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokens = refreshTokens;
        this.emisor = emisor;
        this.duracionAcceso = Duration.ofMinutes(minutosAcceso);
        this.duracionRefresco = Duration.ofDays(diasRefresco);
    }

    public long segundosAcceso() {
        return duracionAcceso.toSeconds();
    }

    public Duration duracionRefresco() {
        return duracionRefresco;
    }

    // Crea el JWT firmado con HS256. Contenido (claims) que leen los otros microservicios:
    //   iss = sonido-vivo, sub = correo, uid = id del usuario, nombre, rol, iat (emitido), exp (expira)
    public String generarAccessToken(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(emisor)
                .subject(usuario.getCorreo())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(duracionAcceso))
                .claim("uid", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol())
                .build();
        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(encabezado, claims)).getTokenValue();
    }

    // Crea un refresh token aleatorio (256 bits). Devuelve el valor real (va a la cookie)
    // y guarda en la base de datos solo su hash.
    @Transactional
    public String crearRefreshToken(Usuario usuario) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String valor = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setTokenHash(hash(valor));
        token.setUsuario(usuario);
        token.setExpiraEn(LocalDateTime.now().plus(duracionRefresco));
        refreshTokens.save(token);
        return valor;
    }

    // Valida un refresh token y lo REVOCA (rotación: cada refresh token sirve una sola vez).
    // Si alguien reutiliza un token ya revocado, probablemente fue robado: se cierran TODAS
    // las sesiones de ese usuario por seguridad.
    @Transactional(noRollbackFor = ErrorNegocio.class)
    public Usuario consumirRefreshToken(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ErrorNegocio(HttpStatus.UNAUTHORIZED, "No hay una sesión activa.");
        }
        RefreshToken token = refreshTokens.findByTokenHash(hash(valor))
                .orElseThrow(() -> new ErrorNegocio(HttpStatus.UNAUTHORIZED, "La sesión no es válida. Inicia sesión nuevamente."));

        Usuario usuario = token.getUsuario();
        if (token.isRevocado()) {
            refreshTokens.revocarTodosDelUsuario(usuario.getId());
            throw new ErrorNegocio(HttpStatus.UNAUTHORIZED, "La sesión fue cerrada. Inicia sesión nuevamente.");
        }
        if (token.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new ErrorNegocio(HttpStatus.UNAUTHORIZED, "La sesión expiró. Inicia sesión nuevamente.");
        }
        if (!usuario.isActivo()) {
            throw new ErrorNegocio(HttpStatus.FORBIDDEN, "Tu cuenta está desactivada. Contacta a la tienda.");
        }
        token.setRevocado(true);
        return usuario;
    }

    // Cerrar sesión: revoca el refresh token (si existe).
    @Transactional
    public void revocar(String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        refreshTokens.findByTokenHash(hash(valor)).ifPresent(token -> token.setRevocado(true));
    }

    @Transactional
    public void revocarTodos(Long usuarioId) {
        refreshTokens.revocarTodosDelUsuario(usuarioId);
    }

    // SHA-256 en hexadecimal (64 caracteres).
    static String hash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
