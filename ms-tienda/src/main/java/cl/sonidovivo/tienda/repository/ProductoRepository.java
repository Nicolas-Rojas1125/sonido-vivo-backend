package cl.sonidovivo.tienda.repository;

import cl.sonidovivo.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    List<Producto> findAllByOrderByCodigoAsc();

    // Descuenta stock de forma ATÓMICA: el UPDATE solo afecta la fila si hay stock suficiente.
    // Devuelve 1 si se descontó y 0 si no alcanzaba. Así dos compras simultáneas nunca dejan stock negativo.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Producto p SET p.stock = p.stock - :cantidad WHERE p.codigo = :codigo AND p.stock >= :cantidad")
    int descontarStock(@Param("codigo") String codigo, @Param("cantidad") int cantidad);
}
