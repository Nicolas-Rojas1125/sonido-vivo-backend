package cl.sonidovivo.contactos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

// Entidad: cada objeto Contacto corresponde a una fila de la tabla "contacto".
// La tabla la crean las migraciones de Flyway (db/migracion); Hibernate solo valida que coincidan.
@Data
@Entity
@Table(name = "contacto")
public class Contacto {

    // Estados posibles de un mensaje.
    public static final String PENDIENTE = "PENDIENTE";
    public static final String ATENDIDO = "ATENDIDO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Las mismas reglas que valida el formulario de React (src/utilidades/validaciones.ts).
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String correo;

    // Opcional: puede venir vacío o null.
    @Pattern(regexp = "^$|^\\+?[0-9 ]{8,15}$", message = "El teléfono no es válido")
    @Column(length = 20)
    private String telefono;

    @NotBlank(message = "El asunto es obligatorio")
    @Pattern(regexp = "stock|pedido|reparacion|otro", message = "El asunto no es válido")
    @Column(nullable = false, length = 30)
    private String asunto;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 10, max = 2000, message = "El mensaje debe tener entre 10 y 2000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    // Estado inicial del mensaje de contacto (columna agregada en la migración V2).
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = PENDIENTE;

    // Vincula el atributo Java con la columna fecha_creacion de MySQL.
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Asigna automáticamente la fecha y hora antes de insertar el contacto.
    @PrePersist
    protected void asignarFechaCreacion() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
