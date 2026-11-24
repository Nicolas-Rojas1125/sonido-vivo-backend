package cl.sonidovivo.usuarios.repository;

import cl.sonidovivo.usuarios.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Revoca TODOS los refresh tokens de un usuario (al desactivarlo o si se detecta un token robado).
    // flush/clear: guarda los cambios pendientes antes del UPDATE y limpia la caché de JPA después,
    // para que las siguientes lecturas vean los tokens ya revocados.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.revocado = true WHERE r.usuario.id = :usuarioId AND r.revocado = false")
    int revocarTodosDelUsuario(@Param("usuarioId") Long usuarioId);
}
