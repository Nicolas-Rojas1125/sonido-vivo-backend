package cl.sonidovivo.tienda.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @Column(length = 30)
    private String id; // ej: guitarras-electricas

    @Column(nullable = false, length = 60)
    private String nombre;
}
