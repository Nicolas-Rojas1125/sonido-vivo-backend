package cl.sonidovivo.tienda.dto;

import jakarta.validation.constraints.*;

// JSON para crear (POST) o editar (PUT) un producto. Mismas reglas que el formulario del panel.
public record ProductoRequest(
        @NotBlank(message = "Ingresa el código.")
        @Pattern(regexp = "^[A-Z]{2}[0-9]{3}$", message = "El código debe tener 2 letras mayúsculas y 3 números (ej: GA006).") String codigo,
        @NotBlank(message = "Selecciona una categoría.") String categoria,
        @NotBlank(message = "Ingresa el nombre.") @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres.") String nombre,
        @NotBlank(message = "Ingresa la marca.") @Size(max = 60) String marca,
        @NotBlank(message = "Ingresa el modelo.") @Size(max = 60) String modelo,
        @NotNull(message = "Ingresa el precio.") @Positive(message = "El precio debe ser mayor a 0.") Integer precio,
        @NotNull(message = "Ingresa el stock.") @PositiveOrZero(message = "El stock no puede ser negativo.") Integer stock,
        @NotBlank(message = "Ingresa una descripción.") @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres.") String descripcion,
        @Size(max = 255) String imagen) {
}
