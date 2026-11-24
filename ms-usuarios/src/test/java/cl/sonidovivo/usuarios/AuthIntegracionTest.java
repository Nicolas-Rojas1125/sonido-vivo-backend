package cl.sonidovivo.usuarios;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// PRUEBAS DE INTEGRACIÓN: levantan la aplicación completa (seguridad + controladores + servicios + JPA + Flyway)
// contra una base MySQL de PRUEBA (sonidovivo_usuarios_test). Requiere Laragon encendido.
// @Transactional: cada prueba se deshace al terminar, así la base de prueba no se ensucia.
@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.datasource.url=jdbc:mysql://localhost:3306/sonidovivo_usuarios_test?createDatabaseIfNotExist=true&serverTimezone=America/Santiago")
class AuthIntegracionTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        // MockMvc simula peticiones HTTP pasando por TODOS los filtros de Spring Security.
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private MvcResult login(String correo, String clave) throws Exception {
        return mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}"))
                .andReturn();
    }

    private String accessToken(MvcResult resultado) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.accessToken");
    }

    // Saca el valor de la cookie sv_refresh del header Set-Cookie.
    private String refreshToken(MvcResult resultado) {
        String setCookie = resultado.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        Matcher m = Pattern.compile("sv_refresh=([^;]*)").matcher(setCookie == null ? "" : setCookie);
        return m.find() ? m.group(1) : "";
    }

    @Test
    @DisplayName("Login correcto: devuelve el JWT con el rol y el refresh token en una cookie httpOnly")
    void loginCorrecto() throws Exception {
        MvcResult resultado = login("admin@sonidovivo.cl", "Admin123");

        assertThat(resultado.getResponse().getStatus()).isEqualTo(200);
        assertThat(accessToken(resultado)).isNotBlank();
        String rol = JsonPath.read(resultado.getResponse().getContentAsString(), "$.usuario.rol");
        assertThat(rol).isEqualTo("ADMINISTRADOR");
        String setCookie = resultado.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("sv_refresh=").contains("HttpOnly").contains("SameSite=Strict");
        // La respuesta NUNCA incluye la contraseña ni su hash.
        assertThat(resultado.getResponse().getContentAsString()).doesNotContain("clave");
    }

    @Test
    @DisplayName("Login con contraseña incorrecta: 401")
    void loginIncorrecto() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@sonidovivo.cl\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos."));
    }

    @Test
    @DisplayName("Sin token no se puede ver la lista de usuarios: 401")
    void sinToken() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un token alterado es rechazado: 401")
    void tokenAlterado() throws Exception {
        String token = accessToken(login("admin@sonidovivo.cl", "Admin123"));
        String alterado = token.substring(0, token.length() - 3) + "abc";
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un CLIENTE no puede gestionar usuarios (403) y el ADMINISTRADOR sí (200)")
    void permisosPorRol() throws Exception {
        String tokenCliente = accessToken(login("cliente@correo.cl", "Cliente123"));
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());

        String tokenAdmin = accessToken(login("admin@sonidovivo.cl", "Admin123"));
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("admin@sonidovivo.cl"))
                .andExpect(jsonPath("$[0].claveHash").doesNotExist());
    }

    @Test
    @DisplayName("/api/auth/yo devuelve el usuario del token")
    void usuarioActual() throws Exception {
        String token = accessToken(login("vendedor@sonidovivo.cl", "Vende123"));
        mvc.perform(get("/api/auth/yo").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("vendedor@sonidovivo.cl"))
                .andExpect(jsonPath("$.rol").value("VENDEDOR"));
    }

    @Test
    @DisplayName("Refresh: entrega un token nuevo y ROTA el refresh token (el anterior ya no sirve)")
    void renovacionConRotacion() throws Exception {
        String refreshOriginal = refreshToken(login("cliente@correo.cl", "Cliente123"));

        MvcResult renovado = mvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("sv_refresh", refreshOriginal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn();
        assertThat(refreshToken(renovado)).isNotBlank().isNotEqualTo(refreshOriginal);

        // Reutilizar el refresh token viejo: rechazado (posible robo -> se cierran todas las sesiones).
        mvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("sv_refresh", refreshOriginal)))
                .andExpect(status().isUnauthorized());
        // Y por eso el token nuevo también quedó revocado.
        mvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("sv_refresh", refreshToken(renovado))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh sin cookie: 401")
    void refreshSinCookie() throws Exception {
        mvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Logout revoca el refresh token")
    void logout() throws Exception {
        String refresh = refreshToken(login("cliente@correo.cl", "Cliente123"));
        mvc.perform(post("/api/auth/logout").cookie(new jakarta.servlet.http.Cookie("sv_refresh", refresh)))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("sv_refresh", refresh)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Registro público: siempre crea un CLIENTE y no permite correos repetidos")
    void registro() throws Exception {
        String json = "{\"rut\":\"7.654.321-6\",\"nombre\":\"Nueva Clienta\",\"correo\":\"nueva@correo.cl\",\"clave\":\"Clave123\"}";
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Registro con RUT inválido o contraseña débil: 400")
    void registroInvalido() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"12.345.678-9\",\"nombre\":\"Ana\",\"correo\":\"ana@correo.cl\",\"clave\":\"Clave123\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"7.654.321-6\",\"nombre\":\"Ana\",\"correo\":\"ana@correo.cl\",\"clave\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.clave").exists());
    }
}
