package cl.sonidovivo.contactos;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// PRUEBAS DE INTEGRACIÓN de ms-contactos contra la base de PRUEBA sonidovivo_contactos_test (Laragon encendido).
@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.datasource.url=jdbc:mysql://localhost:3306/sonidovivo_contactos_test?createDatabaseIfNotExist=true&serverTimezone=America/Santiago")
class ContactosIntegracionTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private static JwtRequestPostProcessor token(String rol) {
        return jwt().jwt(j -> j.subject("prueba@correo.cl").claim("uid", 3L).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    private static final String MENSAJE = """
            {"nombreCompleto":"Tomás Vera","correo":"tomas@correo.cl","telefono":"","asunto":"stock",
             "mensaje":"¿Tienen stock del pedal Boss DS-1?"}""";

    @Test
    @DisplayName("Enviar un mensaje es público: un invitado (sin token) y un cliente reciben 201; el invitado no puede leer mensajes")
    void enviar() throws Exception {
        mvc.perform(post("/api/contactos").contentType(MediaType.APPLICATION_JSON).content(MENSAJE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
        mvc.perform(get("/api/contactos")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/contactos").with(token("CLIENTE")).contentType(MediaType.APPLICATION_JSON).content(MENSAJE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("Mensaje inválido: 400 con el detalle por campo")
    void invalido() throws Exception {
        mvc.perform(post("/api/contactos").with(token("CLIENTE")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"A\",\"correo\":\"malo\",\"asunto\":\"otro\",\"mensaje\":\"corto\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").exists());
    }

    @Test
    @DisplayName("El CLIENTE no puede ver los mensajes; el VENDEDOR sí, y puede atenderlos y eliminarlos")
    void gestion() throws Exception {
        String creado = mvc.perform(post("/api/contactos").with(token("CLIENTE")).contentType(MediaType.APPLICATION_JSON).content(MENSAJE))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(creado, "$.id");

        mvc.perform(get("/api/contactos").with(token("CLIENTE"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/contactos").with(token("VENDEDOR"))).andExpect(status().isOk());

        mvc.perform(put("/api/contactos/" + id + "/atendido").with(token("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDO"));

        mvc.perform(delete("/api/contactos/" + id).with(token("ADMINISTRADOR"))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/contactos/" + id).with(token("ADMINISTRADOR"))).andExpect(status().isNotFound());
    }
}
