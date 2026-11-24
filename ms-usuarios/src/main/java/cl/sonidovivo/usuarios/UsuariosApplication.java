package cl.sonidovivo.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Microservicio de usuarios y autenticación de Sonido Vivo (puerto 8081).
// Emite los tokens JWT que usan ms-tienda y ms-contactos.
@SpringBootApplication
public class UsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosApplication.class, args);
    }
}
