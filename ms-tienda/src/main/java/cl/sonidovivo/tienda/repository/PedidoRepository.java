package cl.sonidovivo.tienda.repository;

import cl.sonidovivo.tienda.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // @EntityGraph trae los items en la misma consulta (evita una consulta extra por cada pedido).
    @EntityGraph(attributePaths = "items")
    List<Pedido> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "items")
    List<Pedido> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    @EntityGraph(attributePaths = "items")
    Optional<Pedido> findWithItemsById(Long id);

    // Resumen de ventas: [estado, cantidad, suma de totales] agrupado por estado.
    @Query("SELECT p.estado, COUNT(p), COALESCE(SUM(p.total), 0) FROM Pedido p GROUP BY p.estado")
    List<Object[]> resumenPorEstado();
}
