package cl.sonidovivo.tienda.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Una línea del pedido: qué producto, a qué precio (el del momento de la compra) y cuántas unidades.
@Getter
@Setter
@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "producto_codigo", nullable = false, length = 5)
    private String productoCodigo;

    @Column(name = "nombre_producto", nullable = false, length = 120)
    private String nombreProducto;

    @Column(name = "precio_unitario", nullable = false)
    private Integer precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;
}
