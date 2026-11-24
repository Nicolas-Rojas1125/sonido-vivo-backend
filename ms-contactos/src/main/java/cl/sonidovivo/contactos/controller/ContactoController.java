package cl.sonidovivo.contactos.controller;

import cl.sonidovivo.contactos.model.Contacto;
import cl.sonidovivo.contactos.service.ContactoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Expone endpoints HTTP y devuelve los datos como JSON.
// Los permisos (quién puede llamar a cada endpoint) y el CORS están en config/SeguridadConfig.
@Tag(name = "Contactos", description = "Enviar mensajes (público, también para invitados) y gestionarlos (ADMINISTRADOR y VENDEDOR)")
@RestController
// Ruta base para las peticiones de contactos.
@RequestMapping("/api/contactos")
public class ContactoController {

    private final ContactoService service;

    // Spring inyecta el servicio mediante el constructor.
    public ContactoController(ContactoService service) {
        this.service = service;
    }

    @Operation(summary = "Enviar un mensaje de contacto", description = "Responde 201 con el mensaje guardado (estado PENDIENTE). 400 si los datos no son válidos.")
    // POST: recibe un JSON, lo valida (@Valid) y registra un nuevo contacto.
    @PostMapping
    public ResponseEntity<Contacto> guardar(@Valid @RequestBody Contacto contacto) {
        Contacto guardado = service.guardar(contacto);

        // Devuelve el contacto guardado y el código HTTP 201.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardado);
    }

    @Operation(summary = "Listar mensajes (ADMINISTRADOR y VENDEDOR)", description = "Los más recientes primero.")
    // GET: devuelve todos los contactos como una lista JSON.
    @GetMapping
    public List<Contacto> listar() {
        return service.listar();
    }

    @Operation(summary = "Ver un mensaje (ADMINISTRADOR y VENDEDOR)", description = "404 si no existe.")
    // GET /api/contactos/{id}: devuelve un contacto o 404 si no existe.
    @GetMapping("/{id}")
    public ResponseEntity<Contacto> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Marcar como ATENDIDO (ADMINISTRADOR y VENDEDOR)", description = "200 con el mensaje actualizado o 404.")
    // PUT /api/contactos/{id}/atendido: actualiza el estado del contacto a ATENDIDO.
    // Responde 200 con el contacto actualizado, o 404 si el id no existe.
    @PutMapping("/{id}/atendido")
    public ResponseEntity<Contacto> marcarAtendido(@PathVariable Long id) {
        return service.marcarAtendido(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar un mensaje (ADMINISTRADOR y VENDEDOR)", description = "204 si se eliminó o 404.")
    // DELETE /api/contactos/{id}: elimina un contacto.
    // Responde 204 (sin contenido) si se eliminó, o 404 si el id no existe.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
