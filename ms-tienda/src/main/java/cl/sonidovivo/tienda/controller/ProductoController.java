package cl.sonidovivo.tienda.controller;

import cl.sonidovivo.tienda.dto.CategoriaResponse;
import cl.sonidovivo.tienda.dto.ProductoRequest;
import cl.sonidovivo.tienda.dto.ProductoResponse;
import cl.sonidovivo.tienda.dto.StockRequest;
import cl.sonidovivo.tienda.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CRUD del catálogo. Los permisos por rol están en SeguridadConfig.
@Tag(name = "Productos", description = "Catálogo (público, también para invitados), CRUD (ADMINISTRADOR) e inventario (ADMINISTRADOR y VENDEDOR)")
@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar productos", description = "Todo el catálogo ordenado por código.")
    @GetMapping("/productos")
    public List<ProductoResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Ver un producto", description = "404 si el código no existe.")
    @GetMapping("/productos/{codigo}")
    public ProductoResponse buscar(@PathVariable String codigo) {
        return service.buscar(codigo);
    }

    @Operation(summary = "Listar categorías")
    @GetMapping("/categorias")
    public List<CategoriaResponse> categorias() {
        return service.listarCategorias();
    }

    @Operation(summary = "Crear producto (ADMINISTRADOR)", description = "Responde 201. 409 si el código ya existe.")
    @PostMapping("/productos")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @Operation(summary = "Editar producto (ADMINISTRADOR)", description = "El código de la URL no se puede cambiar.")
    @PutMapping("/productos/{codigo}")
    public ProductoResponse actualizar(@PathVariable String codigo, @Valid @RequestBody ProductoRequest datos) {
        return service.actualizar(codigo, datos);
    }

    @Operation(summary = "Eliminar producto (ADMINISTRADOR)", description = "Responde 204. Los pedidos antiguos conservan el nombre y precio del producto.")
    @DeleteMapping("/productos/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        service.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualizar stock (ADMINISTRADOR y VENDEDOR)")
    @PatchMapping("/productos/{codigo}/stock")
    public ProductoResponse actualizarStock(@PathVariable String codigo, @Valid @RequestBody StockRequest datos) {
        return service.actualizarStock(codigo, datos.stock());
    }
}
