package cl.sonidovivo.tienda.dto;

import cl.sonidovivo.tienda.model.Pedido;

import java.util.List;

// Mismo formato que la interfaz Pedido del frontend (src/tipos/Pedido.ts).
public record PedidoResponse(Long id, String numero, String fecha, String clienteCorreo, String clienteNombre, String telefono,
                             List<Item> items, String tipoEntrega, String direccion, String comuna, String puntoRetiro,
                             String medioPago, Integer subtotal, Integer costoEnvio, Integer total, String estado,
                             String seguimiento) {

    public record Item(String codigo, String nombre, Integer precio, Integer cantidad) {
    }

    public static PedidoResponse desde(Pedido p) {
        List<Item> items = p.getItems().stream()
                .map(d -> new Item(d.getProductoCodigo(), d.getNombreProducto(), d.getPrecioUnitario(), d.getCantidad()))
                .toList();
        return new PedidoResponse(p.getId(), "SV-" + String.format("%04d", p.getId()), p.getFecha().toString(),
                p.getClienteCorreo(), p.getClienteNombre(), p.getTelefono(), items, p.getTipoEntrega(), p.getDireccion(),
                p.getComuna(), p.getPuntoRetiro(), p.getMedioPago(), p.getSubtotal(), p.getCostoEnvio(), p.getTotal(),
                p.getEstado(), p.getSeguimiento());
    }
}
