package cl.sonidovivo.tienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Microservicio de la tienda (puerto 8083): catálogo, inventario y pedidos.
// Productos y pedidos van juntos porque una compra debe descontar el stock en la MISMA transacción.
@SpringBootApplication
public class TiendaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiendaApplication.class, args);
    }
}
