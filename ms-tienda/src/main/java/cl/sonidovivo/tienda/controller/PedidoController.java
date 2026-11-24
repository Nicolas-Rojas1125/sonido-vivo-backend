package cl.sonidovivo.tienda.controller;

import cl.sonidovivo.tienda.dto.EstadoPedidoRequest;
import cl.sonidovivo.tienda.dto.PedidoRequest;
import cl.sonidovivo.tienda.dto.PedidoResponse;
import cl.sonidovivo.tienda.dto.ResumenResponse;
import cl.sonidovivo.tienda.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Pedidos. @AuthenticationPrincipal Jwt entrega el token ya verificado: de ahí sale QUIÉN hace la petición.
@Tag(name = "Pedidos", description = "Compra (CLIENTE), mis pedidos (CLIENTE), gestión y resumen (ADMINISTRADOR y VENDEDOR)")
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @Operation(summary = "Comprar (CLIENTE)",
            description = "Crea el pedido con los precios de la base de datos y descuenta el stock en una sola transacción. "
                    + "201 si se creó; 409 si algún producto no tiene stock suficiente (no se descuenta nada).")
    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PedidoRequest datos) {
        PedidoService.Cliente cliente = new PedidoService.Cliente(Tokens.idUsuario(jwt), jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(cliente, datos));
    }

    @Operation(summary = "Mis pedidos (CLIENTE)", description = "Solo los pedidos del usuario del token.")
    @GetMapping("/mios")
    public List<PedidoResponse> misPedidos(@AuthenticationPrincipal Jwt jwt) {
        return service.listarDelCliente(Tokens.idUsuario(jwt));
    }

    @Operation(summary = "Todos los pedidos (ADMINISTRADOR y VENDEDOR)")
    @GetMapping
    public List<PedidoResponse> listar() {
        return service.listarTodos();
    }

    @Operation(summary = "Resumen de ventas (ADMINISTRADOR y VENDEDOR)")
    @GetMapping("/resumen")
    public ResumenResponse resumen() {
        return service.resumen();
    }

    @Operation(summary = "Ver un pedido", description = "El cliente solo puede ver los suyos (403 si no); el personal ve todos.")
    @GetMapping("/{id}")
    public PedidoResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.buscar(id, Tokens.idUsuario(jwt), Tokens.esPersonal(jwt));
    }

    @Operation(summary = "Cambiar estado y seguimiento (ADMINISTRADOR y VENDEDOR)",
            description = "Estados: preparacion -> despachado -> entregado.")
    @PutMapping("/{id}/estado")
    public PedidoResponse actualizarEstado(@PathVariable Long id, @Valid @RequestBody EstadoPedidoRequest datos) {
        return service.actualizarEstado(id, datos);
    }
}
