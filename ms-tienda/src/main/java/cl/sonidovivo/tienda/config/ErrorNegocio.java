package cl.sonidovivo.tienda.config;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// Error de la lógica de negocio con su código HTTP. Ej: new ErrorNegocio(HttpStatus.CONFLICT, "Ya existe...")
// ManejadorErrores lo convierte en una respuesta JSON: { "mensaje": "Ya existe..." }
@Getter
public class ErrorNegocio extends RuntimeException {

    private final HttpStatus estado;

    public ErrorNegocio(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }
}
