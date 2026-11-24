package cl.sonidovivo.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// JSON que recibe POST /api/auth/registro (registro público: siempre crea un CLIENTE).
public record RegistroRequest(
        @NotBlank(message = "Ingresa tu RUT.") String rut,
        @NotBlank(message = "Ingresa tu nombre.") @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres.") String nombre,
        @NotBlank(message = "Ingresa tu correo.") @Email(message = "Ingresa un correo electrónico válido.") @Size(max = 150) String correo,
        @NotBlank(message = "Ingresa una contraseña.")
        @Pattern(regexp = UsuarioRequest.REGEX_CLAVE, message = UsuarioRequest.MENSAJE_CLAVE) String clave) {
}
