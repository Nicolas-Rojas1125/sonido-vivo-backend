package cl.sonidovivo.tienda.model;

import jakarta.persistence.*;
import lombok.Data;

// Producto del catálogo. El código del Excel (GA001...) es la clave primaria.
@Data
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @Column(length = 5)
    private String codigo;

    // Muchos productos pertenecen a una categoría (FOREIGN KEY categoria_id).
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 60)
    private String modelo;

    @Column(nullable = false)
    private Integer precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 255)
    private String imagen;
}
