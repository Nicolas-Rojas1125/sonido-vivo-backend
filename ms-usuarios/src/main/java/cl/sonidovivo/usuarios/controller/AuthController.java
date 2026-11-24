package cl.sonidovivo.usuarios.controller;

import cl.sonidovivo.usuarios.dto.LoginRequest;
import cl.sonidovivo.usuarios.dto.RegistroRequest;
import cl.sonidovivo.usuarios.dto.SesionResponse;
import cl.sonidovivo.usuarios.dto.UsuarioResponse;
import cl.sonidovivo.usuarios.model.Usuario;
import cl.sonidovivo.usuarios.service.AuthService;
import cl.sonidovivo.usuarios.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

// Autenticación con JWT.
@Tag(name = "Autenticación", description = "Login, registro, renovación (refresh) y cierre de sesión")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Nombre y ruta de la cookie del refresh token. Path=/api/auth: el navegador solo la envía a estos endpoints.
    static final String COOKIE_REFRESH = "sv_refresh";
    private static final String RUTA_COOKIE = "/api/auth";

    private final AuthService authService;
    private final TokenService tokenService;
    private final boolean cookieSegura;

    public AuthController(AuthService authService, TokenService tokenService,
                          @Value("${jwt.cookie-segura}") boolean cookieSegura) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.cookieSegura = cookieSegura;
    }

    @Operation(summary = "Iniciar sesión",
            description = "Valida correo y contraseña (BCrypt). Devuelve el access token (JWT, 15 min) en el cuerpo "
                    + "y el refresh token (7 días) en la cookie httpOnly sv_refresh. 401 si las credenciales no son válidas.")
    @SecurityRequirements // público: no necesita token
    @PostMapping("/login")
    public ResponseEntity<SesionResponse> login(@Valid @RequestBody LoginRequest datos) {
        Usuario usuario = authService.autenticar(datos);
        return respuestaConSesion(HttpStatus.OK, usuario);
    }

    @Operation(summary = "Registrarse como cliente",
            description = "Registro público: siempre crea una cuenta con rol CLIENTE y deja la sesión iniciada. 409 si el correo o RUT ya existen.")
    @SecurityRequirements
    @PostMapping("/registro")
    public ResponseEntity<SesionResponse> registro(@Valid @RequestBody RegistroRequest datos) {
        Usuario usuario = authService.registrar(datos);
        return respuestaConSesion(HttpStatus.CREATED, usuario);
    }

    @Operation(summary = "Renovar el access token",
            description = "Usa la cookie sv_refresh para entregar un nuevo access token. El refresh token se ROTA "
                    + "(el anterior deja de servir). Lo usa el frontend al recargar la página y cuando el token expira.")
    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<SesionResponse> refresh(@CookieValue(name = COOKIE_REFRESH, required = false) String refreshToken) {
        Usuario usuario = tokenService.consumirRefreshToken(refreshToken);
        return respuestaConSesion(HttpStatus.OK, usuario);
    }

    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token y borra la cookie. Responde 204.")
    @SecurityRequirements
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = COOKIE_REFRESH, required = false) String refreshToken) {
        tokenService.revocar(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                .build();
    }

    @Operation(summary = "Usuario actual", description = "Devuelve los datos del usuario dueño del token (requiere Authorization: Bearer).")
    @GetMapping("/yo")
    public UsuarioResponse yo(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioResponse.desde(authService.usuarioActual(Tokens.idUsuario(jwt)));
    }

    // Arma la respuesta: access token en el JSON + refresh token nuevo en la cookie.
    private ResponseEntity<SesionResponse> respuestaConSesion(HttpStatus estado, Usuario usuario) {
        String accessToken = tokenService.generarAccessToken(usuario);
        String refreshToken = tokenService.crearRefreshToken(usuario);
        SesionResponse cuerpo = new SesionResponse(accessToken, "Bearer", tokenService.segundosAcceso(), UsuarioResponse.desde(usuario));
        return ResponseEntity.status(estado)
                .header(HttpHeaders.SET_COOKIE, cookie(refreshToken, tokenService.duracionRefresco()).toString())
                .body(cuerpo);
    }

    // httpOnly: JavaScript NO puede leerla (protege contra robo por XSS).
    // SameSite=Strict: el navegador no la envía desde otros sitios (protege contra CSRF).
    private ResponseCookie cookie(String valor, Duration duracion) {
        return ResponseCookie.from(COOKIE_REFRESH, valor)
                .httpOnly(true)
                .secure(cookieSegura)
                .sameSite("Strict")
                .path(RUTA_COOKIE)
                .maxAge(duracion)
                .build();
    }
}
