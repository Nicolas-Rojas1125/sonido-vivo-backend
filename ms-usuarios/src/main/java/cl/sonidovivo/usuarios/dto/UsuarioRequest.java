package cl.sonidovivo.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// JSON que recibe el Administrador para crear (POST) o editar (PUT) un usuario.
// Al editar, "clave" puede venir vacía: se mantiene la contraseña actual.
public record UsuarioRequest(
        @NotBlank(message = "Ingresa el RUT.") String rut,
        @NotBlank(message = "Ingresa el nombre.") @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres.") String nombre,
        @NotBlank(message = "Ingresa el correo.") @Email(message = "Ingresa un correo electrónico válido.") @Size(max = 150) String correo,
        @Pattern(regexp = "^$|" + REGEX_CLAVE, message = MENSAJE_CLAVE) String clave,
        @NotBlank(message = "Selecciona un rol.")
        @Pattern(regexp = "ADMINISTRADOR|VENDEDOR|CLIENTE", message = "El rol debe ser ADMINISTRADOR, VENDEDOR o CLIENTE.") String rol) {

    // Mínimo 8 caracteres, con mayúscula, minúscula y número (misma regla que el frontend).
    public static final String REGEX_CLAVE = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,72}$";
    public static final String MENSAJE_CLAVE = "La contraseña debe tener al menos 8 caracteres, con mayúscula, minúscula y número.";
}
