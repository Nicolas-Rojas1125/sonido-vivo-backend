package cl.sonidovivo.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// JSON de PUT /api/pedidos/{id}/estado -> { "estado": "despachado", "seguimiento": "CHX-998877" }
public record EstadoPedidoRequest(
        @NotBlank @Pattern(regexp = "preparacion|despachado|entregado", message = "Estado no válido.") String estado,
        @Size(max = 40, message = "El número de seguimiento no puede superar los 40 caracteres.") String seguimiento) {
}
