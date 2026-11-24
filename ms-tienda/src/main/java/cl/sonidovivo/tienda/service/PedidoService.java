package cl.sonidovivo.tienda.service;

import cl.sonidovivo.tienda.config.ErrorNegocio;
import cl.sonidovivo.tienda.dto.EstadoPedidoRequest;
import cl.sonidovivo.tienda.dto.ItemPedidoRequest;
import cl.sonidovivo.tienda.dto.PedidoRequest;
import cl.sonidovivo.tienda.dto.PedidoResponse;
import cl.sonidovivo.tienda.dto.ResumenResponse;
import cl.sonidovivo.tienda.model.DetallePedido;
import cl.sonidovivo.tienda.model.Pedido;
import cl.sonidovivo.tienda.model.Producto;
import cl.sonidovivo.tienda.repository.PedidoRepository;
import cl.sonidovivo.tienda.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

// Lógica de las compras.
@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final ProductoRepository productos;
    private final int costoDespacho;
    private final Set<String> puntosRetiro;

    public PedidoService(PedidoRepository pedidos, ProductoRepository productos,
                         @Value("${tienda.costo-despacho}") int costoDespacho,
                         @Value("${tienda.puntos-retiro}") String[] puntosRetiro) {
        this.pedidos = pedidos;
        this.productos = productos;
        this.costoDespacho = costoDespacho;
        this.puntosRetiro = Set.of(puntosRetiro);
    }

    // Crea el pedido y descuenta el stock en UNA transacción: si un producto no alcanza,
    // se lanza un error y se deshace TODO (no queda ni el pedido ni stock descontado a medias).
    @Transactional
    public PedidoResponse crear(Cliente cliente, PedidoRequest datos) {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId(cliente.id());
        pedido.setClienteCorreo(cliente.correo());
        pedido.setClienteNombre(datos.clienteNombre().trim());
        pedido.setTelefono(datos.telefono().trim());
        pedido.setMedioPago(datos.medioPago());
        pedido.setTipoEntrega(datos.tipoEntrega());
        asignarEntrega(pedido, datos);

        // Junta cantidades si el mismo código viene repetido; TreeMap ordena por código
        // (descontar siempre en el mismo orden evita bloqueos entre compras simultáneas).
        Map<String, Integer> cantidades = new TreeMap<>();
        for (ItemPedidoRequest item : datos.items()) {
            cantidades.merge(item.codigo().trim().toUpperCase(), item.cantidad(), Integer::sum);
        }

        int subtotal = 0;
        for (Map.Entry<String, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productos.findById(entrada.getKey())
                    .orElseThrow(() -> new ErrorNegocio(HttpStatus.CONFLICT,
                            "El producto " + entrada.getKey() + " ya no existe en el catálogo."));
            int cantidad = entrada.getValue();
            if (productos.descontarStock(producto.getCodigo(), cantidad) == 0) {
                throw new ErrorNegocio(HttpStatus.CONFLICT,
                        "Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getStock() + ".");
            }
            DetallePedido detalle = new DetallePedido();
            detalle.setProductoCodigo(producto.getCodigo());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setPrecioUnitario(producto.getPrecio()); // precio de la BASE DE DATOS
            detalle.setCantidad(cantidad);
            pedido.agregarItem(detalle);
            subtotal += producto.getPrecio() * cantidad;
        }

        // Despacho a domicilio tiene costo fijo; el retiro es gratis.
        int envio = "despacho".equals(datos.tipoEntrega()) ? costoDespacho : 0;
        pedido.setSubtotal(subtotal);
        pedido.setCostoEnvio(envio);
        pedido.setTotal(subtotal + envio);
        return PedidoResponse.desde(pedidos.save(pedido));
    }

    // Un cliente solo ve SUS pedidos (filtrados por el id de su token).
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarDelCliente(Long usuarioId) {
        return pedidos.findByUsuarioIdOrderByIdDesc(usuarioId).stream().map(PedidoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidos.findAllByOrderByIdDesc().stream().map(PedidoResponse::desde).toList();
    }

    // El personal de la tienda ve cualquier pedido; un cliente, solo los suyos (si no: 403).
    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id, Long usuarioId, boolean esPersonal) {
        Pedido pedido = obtener(id);
        if (!esPersonal && !usuarioId.equals(pedido.getUsuarioId())) {
            throw new ErrorNegocio(HttpStatus.FORBIDDEN, "No puedes ver pedidos de otros clientes.");
        }
        return PedidoResponse.desde(pedido);
    }

    @Transactional
    public PedidoResponse actualizarEstado(Long id, EstadoPedidoRequest datos) {
        Pedido pedido = obtener(id);
        pedido.setEstado(datos.estado());
        pedido.setSeguimiento(datos.seguimiento() == null ? "" : datos.seguimiento().trim());
        return PedidoResponse.desde(pedido);
    }

    @Transactional(readOnly = true)
    public ResumenResponse resumen() {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        porEstado.put(Pedido.PREPARACION, 0L);
        porEstado.put(Pedido.DESPACHADO, 0L);
        porEstado.put(Pedido.ENTREGADO, 0L);
        long total = 0;
        long cantidad = 0;
        for (Object[] fila : pedidos.resumenPorEstado()) {
            long n = ((Number) fila[1]).longValue();
            porEstado.put((String) fila[0], n);
            cantidad += n;
            total += ((Number) fila[2]).longValue();
        }
        return new ResumenResponse(total, cantidad, porEstado);
    }

    private void asignarEntrega(Pedido pedido, PedidoRequest datos) {
        if ("despacho".equals(datos.tipoEntrega())) {
            if (vacio(datos.direccion()) || vacio(datos.comuna())) {
                throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "Para despacho a domicilio ingresa dirección y comuna.");
            }
            pedido.setDireccion(datos.direccion().trim());
            pedido.setComuna(datos.comuna().trim());
            pedido.setPuntoRetiro("");
        } else {
            if (vacio(datos.puntoRetiro()) || !puntosRetiro.contains(datos.puntoRetiro())) {
                throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "Selecciona un punto de retiro válido.");
            }
            pedido.setPuntoRetiro(datos.puntoRetiro());
            pedido.setDireccion("");
            pedido.setComuna("");
        }
    }

    private Pedido obtener(Long id) {
        return pedidos.findWithItemsById(id)
                .orElseThrow(() -> new ErrorNegocio(HttpStatus.NOT_FOUND, "El pedido no existe."));
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    // Datos del cliente que vienen del token JWT.
    public record Cliente(Long id, String correo) {
    }
}
