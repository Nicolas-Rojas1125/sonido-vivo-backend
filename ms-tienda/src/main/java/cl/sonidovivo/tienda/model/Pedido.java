package cl.sonidovivo.tienda.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Pedido de un cliente. Con @Getter/@Setter (no @Data) para evitar ciclos en toString/hashCode
// por la relación de ida y vuelta con DetallePedido.
@Getter
@Setter
@Entity
@Table(name = "pedido")
public class Pedido {

    public static final String PREPARACION = "preparacion";
    public static final String DESPACHADO = "despachado";
    public static final String ENTREGADO = "entregado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // id del cliente en ms-usuarios (viene del claim "uid" del token, NO del JSON).
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "cliente_nombre", nullable = false, length = 120)
    private String clienteNombre;

    @Column(name = "cliente_correo", nullable = false, length = 150)
    private String clienteCorreo;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "tipo_entrega", nullable = false, length = 10)
    private String tipoEntrega;

    @Column(nullable = false, length = 150)
    private String direccion = "";

    @Column(nullable = false, length = 60)
    private String comuna = "";

    @Column(name = "punto_retiro", nullable = false, length = 30)
    private String puntoRetiro = "";

    @Column(name = "medio_pago", nullable = false, length = 15)
    private String medioPago;

    @Column(nullable = false)
    private Integer subtotal;

    @Column(name = "costo_envio", nullable = false)
    private Integer costoEnvio;

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = false, length = 15)
    private String estado = PREPARACION;

    @Column(nullable = false, length = 40)
    private String seguimiento = "";

    // Un pedido tiene muchos detalles; se guardan y eliminan junto con el pedido (cascade).
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetallePedido> items = new ArrayList<>();

    @PrePersist
    protected void asignarFecha() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public void agregarItem(DetallePedido item) {
        item.setPedido(this);
        items.add(item);
    }
}
