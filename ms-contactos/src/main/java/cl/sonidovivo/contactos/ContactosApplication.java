package cl.sonidovivo.contactos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio de contactos de Sonido Vivo.
// Al iniciar: Flyway ejecuta las migraciones pendientes, Hibernate valida las tablas
// y Tomcat queda escuchando en http://localhost:8082.
@SpringBootApplication
public class ContactosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContactosApplication.class, args);
    }
}
