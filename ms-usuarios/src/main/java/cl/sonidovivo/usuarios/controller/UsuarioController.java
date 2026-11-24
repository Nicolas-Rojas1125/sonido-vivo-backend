package cl.sonidovivo.usuarios.controller;

import cl.sonidovivo.usuarios.dto.EstadoRequest;
import cl.sonidovivo.usuarios.dto.UsuarioRequest;
import cl.sonidovivo.usuarios.dto.UsuarioResponse;
import cl.sonidovivo.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Gestión de usuarios. TODOS estos endpoints exigen rol ADMINISTRADOR (ver SeguridadConfig).
@Tag(name = "Usuarios", description = "CRUD de usuarios y roles (solo ADMINISTRADOR)")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Listar usuarios")
    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Buscar un usuario por id", description = "404 si no existe.")
    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @Operation(summary = "Crear usuario", description = "Crea un usuario con cualquier rol. Responde 201. 409 si el correo o RUT ya existen.")
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @Operation(summary = "Editar usuario y asignar rol", description = "Si la clave viene vacía se mantiene la actual. No puedes cambiar tu propio rol.")
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody UsuarioRequest datos) {
        return service.actualizar(Tokens.idUsuario(jwt), id, datos);
    }

    @Operation(summary = "Activar o desactivar una cuenta",
            description = "Las cuentas no se eliminan (sus pedidos quedan como historial): se desactivan y se cierran sus sesiones.")
    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody EstadoRequest datos) {
        return service.cambiarEstado(Tokens.idUsuario(jwt), id, datos.activo());
    }
}
