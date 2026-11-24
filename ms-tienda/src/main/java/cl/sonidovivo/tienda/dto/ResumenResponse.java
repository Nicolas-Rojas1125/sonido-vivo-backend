package cl.sonidovivo.tienda.dto;

import java.util.Map;

// Resumen para el panel: total vendido, cantidad de pedidos y pedidos por estado.
public record ResumenResponse(long totalVentas, long cantidadPedidos, Map<String, Long> porEstado) {
}
