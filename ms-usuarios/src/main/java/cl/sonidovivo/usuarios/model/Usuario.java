package cl.sonidovivo.usuarios.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// Entidad: cada objeto Usuario es una fila de la tabla "usuario" (creada por Flyway, V1).
@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    // Roles del caso Sonido Vivo.
    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String VENDEDOR = "VENDEDOR";
    public static final String CLIENTE = "CLIENTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12, unique = true)
    private String rut;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 150, unique = true)
    private String correo;

    // Nunca se guarda ni se devuelve la contraseña real: solo su hash BCrypt.
    @Column(name = "clave_hash", nullable = false, length = 100)
    private String claveHash;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void asignarFechaCreacion() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
