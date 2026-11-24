package cl.sonidovivo.tienda.repository;

import cl.sonidovivo.tienda.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, String> {
}
