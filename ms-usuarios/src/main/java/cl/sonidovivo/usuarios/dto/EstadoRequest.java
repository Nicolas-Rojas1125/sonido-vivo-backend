package cl.sonidovivo.usuarios.dto;

import jakarta.validation.constraints.NotNull;

// JSON de PATCH /api/usuarios/{id}/estado -> { "activo": false }
public record EstadoRequest(@NotNull(message = "Indica si la cuenta queda activa.") Boolean activo) {
}
