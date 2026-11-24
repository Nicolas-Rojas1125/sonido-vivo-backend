package cl.sonidovivo.usuarios.repository;

import cl.sonidovivo.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Consultas generadas por Spring Data a partir del nombre de cada método.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    List<Usuario> findAllByOrderByIdAsc();

    boolean existsByCorreo(String correo);

    boolean existsByRut(String rut);

    // Para editar: ¿otro usuario (distinto id) ya usa ese correo o RUT?
    boolean existsByCorreoAndIdNot(String correo, Long id);

    boolean existsByRutAndIdNot(String rut, Long id);
}
