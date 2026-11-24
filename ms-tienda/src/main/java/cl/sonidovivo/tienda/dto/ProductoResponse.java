package cl.sonidovivo.tienda.dto;

import cl.sonidovivo.tienda.model.Producto;

// Mismo formato que la interfaz Producto del frontend (src/tipos/Producto.ts).
public record ProductoResponse(String codigo, String categoria, String nombre, String marca, String modelo,
                               Integer stock, Integer precio, String descripcion, String imagen) {

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(p.getCodigo(), p.getCategoria().getId(), p.getNombre(), p.getMarca(), p.getModelo(),
                p.getStock(), p.getPrecio(), p.getDescripcion(), p.getImagen());
    }
}
