package cl.sonidovivo.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// JSON que recibe POST /api/auth/login
public record LoginRequest(
        @NotBlank(message = "Ingresa tu correo.") @Email(message = "Ingresa un correo electrónico válido.") String correo,
        @NotBlank(message = "Ingresa tu contraseña.") String clave) {
}
