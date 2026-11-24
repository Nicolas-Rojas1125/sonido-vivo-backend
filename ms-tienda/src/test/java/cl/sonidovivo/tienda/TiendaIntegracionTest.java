package cl.sonidovivo.tienda;

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

// PRUEBAS DE INTEGRACIÓN de ms-tienda contra la base de PRUEBA sonidovivo_tienda_test (Laragon encendido).
// jwt() simula un token ya verificado con el rol indicado (no hace falta levantar ms-usuarios).
@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.datasource.url=jdbc:mysql://localhost:3306/sonidovivo_tienda_test?createDatabaseIfNotExist=true&serverTimezone=America/Santiago")
class TiendaIntegracionTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    // Token simulado con los mismos claims que emite ms-usuarios.
    private static JwtRequestPostProcessor token(long uid, String correo, String rol) {
        return jwt().jwt(j -> j.subject(correo).claim("uid", uid).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    private static JwtRequestPostProcessor cliente() {
        return token(3, "cliente@correo.cl", "CLIENTE");
    }

    private static JwtRequestPostProcessor vendedor() {
        return token(2, "vendedor@sonidovivo.cl", "VENDEDOR");
    }

    private static JwtRequestPostProcessor admin() {
        return token(1, "admin@sonidovivo.cl", "ADMINISTRADOR");
    }

    private static final String PRODUCTO_NUEVO = """
            {"codigo":"GA099","categoria":"guitarras-acusticas","nombre":"Guitarra de Prueba","marca":"Marca",
             "modelo":"M1","precio":100000,"stock":3,"descripcion":"Producto creado en una prueba."}""";

    @Test
    @DisplayName("El catálogo es público: un invitado (sin token) ve los 51 productos; un cliente también")
    void catalogo() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(51));
        mvc.perform(get("/api/categorias")).andExpect(status().isOk());
        mvc.perform(get("/api/productos").with(cliente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(51))
                .andExpect(jsonPath("$[0].codigo").exists());
        mvc.perform(get("/api/productos/NO999").with(cliente())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("El invitado NO puede comprar, ver pedidos ni modificar productos (401 sin token)")
    void invitadoSinPermisos() throws Exception {
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pedidos/mios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO)).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/productos/GA001")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CRUD de productos: solo el ADMINISTRADOR crea, edita y elimina")
    void crudProductos() throws Exception {
        mvc.perform(post("/api/productos").with(vendedor()).contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/productos").with(admin()).contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagen").value("/imagenes/productos/GA099.jpg"));
        mvc.perform(post("/api/productos").with(admin()).contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isConflict());

        mvc.perform(put("/api/productos/GA099").with(admin()).contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCTO_NUEVO.replace("100000", "120000")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(120000));

        mvc.perform(delete("/api/productos/GA099").with(admin())).andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/GA099").with(admin())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Datos inválidos de producto: 400 con el detalle por campo")
    void productoInvalido() throws Exception {
        mvc.perform(post("/api/productos").with(admin()).contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCTO_NUEVO.replace("\"GA099\"", "\"ga9\"").replace("100000", "-5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigo").exists())
                .andExpect(jsonPath("$.errores.precio").exists());
    }

    @Test
    @DisplayName("Inventario: el VENDEDOR puede cambiar el stock, el CLIENTE no")
    void inventario() throws Exception {
        mvc.perform(patch("/api/productos/GA001/stock").with(cliente()).contentType(MediaType.APPLICATION_JSON).content("{\"stock\":20}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/productos/GA001/stock").with(vendedor()).contentType(MediaType.APPLICATION_JSON).content("{\"stock\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(20));
    }

    @Test
    @DisplayName("Compra: usa el precio de la base de datos (ignora un precio enviado) y descuenta stock")
    void compra() throws Exception {
        String pedido = """
                {"clienteNombre":"Cliente de Prueba","telefono":"+56 9 1111 2222","tipoEntrega":"despacho",
                 "direccion":"Calle 1","comuna":"Providencia","puntoRetiro":"","medioPago":"webpay",
                 "items":[{"codigo":"GA001","cantidad":2,"precio":1}]}""";
        mvc.perform(post("/api/pedidos").with(cliente()).contentType(MediaType.APPLICATION_JSON).content(pedido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteCorreo").value("cliente@correo.cl"))
                .andExpect(jsonPath("$.items[0].precio").value(129990))
                .andExpect(jsonPath("$.subtotal").value(259980))
                .andExpect(jsonPath("$.costoEnvio").value(5990))
                .andExpect(jsonPath("$.total").value(265970))
                .andExpect(jsonPath("$.estado").value("preparacion"));

        // Stock inicial de GA001 = 8 -> quedan 6
        mvc.perform(get("/api/productos/GA001").with(cliente())).andExpect(jsonPath("$.stock").value(6));
    }

    @Test
    @DisplayName("Compra sin stock suficiente: 409 y NO se descuenta nada (todo o nada)")
    void compraSinStock() throws Exception {
        String pedido = """
                {"clienteNombre":"Cliente de Prueba","telefono":"+56 9 1111 2222","tipoEntrega":"retiro",
                 "puntoRetiro":"tienda","medioPago":"transferencia",
                 "items":[{"codigo":"GA001","cantidad":1},{"codigo":"GE005","cantidad":50}]}""";
        mvc.perform(post("/api/pedidos").with(cliente()).contentType(MediaType.APPLICATION_JSON).content(pedido))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Stock insuficiente")));
    }

    @Test
    @DisplayName("Solo el CLIENTE puede comprar")
    void soloClienteCompra() throws Exception {
        mvc.perform(post("/api/pedidos").with(vendedor()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cada cliente ve solo sus pedidos; el personal ve todos")
    void verPedidos() throws Exception {
        mvc.perform(get("/api/pedidos/mios").with(cliente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].numero").value("SV-0002"));

        mvc.perform(get("/api/pedidos").with(cliente())).andExpect(status().isForbidden());
        mvc.perform(get("/api/pedidos/3").with(cliente())).andExpect(status().isForbidden()); // pedido de otra persona
        mvc.perform(get("/api/pedidos/1").with(cliente())).andExpect(status().isOk());

        mvc.perform(get("/api/pedidos").with(vendedor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("El vendedor actualiza estado y seguimiento; el resumen suma las ventas")
    void gestionPedidos() throws Exception {
        mvc.perform(put("/api/pedidos/3/estado").with(vendedor()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"despachado\",\"seguimiento\":\"CHX-998877\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("despachado"))
                .andExpect(jsonPath("$.seguimiento").value("CHX-998877"));

        mvc.perform(get("/api/pedidos/resumen").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentas").value(268930))
                .andExpect(jsonPath("$.cantidadPedidos").value(3));
    }
}
