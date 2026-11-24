package cl.sonidovivo.contactos.repository;

import cl.sonidovivo.contactos.model.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Proporciona las operaciones de acceso a la base de datos (save, findAll, findById, deleteById...).
// Contacto: entidad que administra el repositorio.
// Long: tipo de dato de la clave primaria (id).
public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    // Spring genera la consulta a partir del nombre del método:
    // SELECT * FROM contacto ORDER BY fecha_creacion DESC
    List<Contacto> findAllByOrderByFechaCreacionDesc();
}
