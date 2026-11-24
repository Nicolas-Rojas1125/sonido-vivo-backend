package cl.sonidovivo.tienda.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Cada producto del carrito: solo código y cantidad. El PRECIO lo pone el servidor
// (desde la base de datos), así nadie puede comprar más barato editando el JSON.
public record ItemPedidoRequest(
        @NotBlank(message = "Falta el código del producto.") String codigo,
        @NotNull @Min(value = 1, message = "La cantidad mínima es 1.") @Max(value = 99, message = "La cantidad máxima es 99.") Integer cantidad) {
}
