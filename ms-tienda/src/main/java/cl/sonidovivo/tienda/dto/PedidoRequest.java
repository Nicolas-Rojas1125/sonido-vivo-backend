package cl.sonidovivo.tienda.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

// JSON de POST /api/pedidos. El correo y el id del cliente se toman del TOKEN, no de aquí.
public record PedidoRequest(
        @NotBlank(message = "Ingresa tu nombre.") @Size(min = 3, max = 120) String clienteNombre,
        @NotBlank(message = "Ingresa tu teléfono.") @Pattern(regexp = "^\\+?[0-9 ]{8,15}$", message = "Ingresa un teléfono válido, por ejemplo +56 9 1234 5678.") String telefono,
        @NotEmpty(message = "El carrito está vacío.") @Size(max = 50) List<@Valid ItemPedidoRequest> items,
        @NotBlank @Pattern(regexp = "despacho|retiro", message = "El tipo de entrega debe ser despacho o retiro.") String tipoEntrega,
        @Size(max = 150) String direccion,
        @Size(max = 60) String comuna,
        @Size(max = 30) String puntoRetiro,
        @NotBlank @Pattern(regexp = "transferencia|webpay", message = "El medio de pago debe ser transferencia o webpay.") String medioPago) {
}
