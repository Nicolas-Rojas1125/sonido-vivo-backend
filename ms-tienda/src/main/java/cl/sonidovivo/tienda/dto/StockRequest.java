package cl.sonidovivo.tienda.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

// JSON de PATCH /api/productos/{codigo}/stock -> { "stock": 12 }
public record StockRequest(
        @NotNull(message = "Ingresa el stock.") @PositiveOrZero(message = "El stock debe ser un número entero mayor o igual a 0.") Integer stock) {
}
