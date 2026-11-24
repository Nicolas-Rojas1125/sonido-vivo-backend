package cl.sonidovivo.usuarios.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// Refresh token guardado en la base de datos (tabla refresh_token, migración V2).
// Solo se guarda el HASH del token; el valor real viaja en una cookie httpOnly.
@Data
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    // Muchos refresh tokens pertenecen a un usuario (uno por cada navegador/dispositivo).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(nullable = false)
    private boolean revocado = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void asignarFechaCreacion() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
