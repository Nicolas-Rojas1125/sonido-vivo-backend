package cl.sonidovivo.contactos.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// Convierte los errores en respuestas JSON con el mismo formato en todos los microservicios:
//   { "mensaje": "texto para mostrar al usuario", "errores": { "campo": "detalle" } }
@RestControllerAdvice
public class ManejadorErrores {

    // Errores de negocio (409 duplicado, 404 no existe, 401 credenciales...).
    @ExceptionHandler(ErrorNegocio.class)
    public ResponseEntity<Map<String, Object>> manejarNegocio(ErrorNegocio ex) {
        return ResponseEntity.status(ex.getEstado()).body(Map.of("mensaje", ex.getMessage()));
    }

    // @Valid falló: 400 con un mensaje por cada campo.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        String primero = errores.values().stream().findFirst().orElse("Datos inválidos.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", primero, "errores", errores));
    }

    // JSON mal formado (ej: texto donde va un número).
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", "El cuerpo de la petición no es un JSON válido."));
    }
}
