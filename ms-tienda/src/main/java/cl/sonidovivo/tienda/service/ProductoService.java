package cl.sonidovivo.tienda.service;

import cl.sonidovivo.tienda.config.ErrorNegocio;
import cl.sonidovivo.tienda.dto.CategoriaResponse;
import cl.sonidovivo.tienda.dto.ProductoRequest;
import cl.sonidovivo.tienda.dto.ProductoResponse;
import cl.sonidovivo.tienda.model.Categoria;
import cl.sonidovivo.tienda.model.Producto;
import cl.sonidovivo.tienda.repository.CategoriaRepository;
import cl.sonidovivo.tienda.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Lógica del catálogo e inventario.
@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public ProductoService(ProductoRepository productos, CategoriaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productos.findAllByOrderByCodigoAsc().stream().map(ProductoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse buscar(String codigo) {
        return ProductoResponse.desde(obtener(codigo));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categorias.findAll().stream().map(c -> new CategoriaResponse(c.getId(), c.getNombre())).toList();
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest datos) {
        if (productos.existsById(datos.codigo())) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "Ya existe un producto con el código " + datos.codigo() + ".");
        }
        Producto producto = new Producto();
        producto.setCodigo(datos.codigo());
        copiarDatos(datos, producto);
        return ProductoResponse.desde(productos.save(producto));
    }

    // El código no se cambia al editar (es el identificador y el nombre de la imagen).
    @Transactional
    public ProductoResponse actualizar(String codigo, ProductoRequest datos) {
        Producto producto = obtener(codigo);
        copiarDatos(datos, producto);
        return ProductoResponse.desde(producto);
    }

    @Transactional
    public void eliminar(String codigo) {
        productos.delete(obtener(codigo));
    }

    @Transactional
    public ProductoResponse actualizarStock(String codigo, int stock) {
        Producto producto = obtener(codigo);
        producto.setStock(stock);
        return ProductoResponse.desde(producto);
    }

    private void copiarDatos(ProductoRequest datos, Producto producto) {
        Categoria categoria = categorias.findById(datos.categoria())
                .orElseThrow(() -> new ErrorNegocio(HttpStatus.BAD_REQUEST, "La categoría no existe."));
        producto.setCategoria(categoria);
        producto.setNombre(datos.nombre().trim());
        producto.setMarca(datos.marca().trim());
        producto.setModelo(datos.modelo().trim());
        producto.setPrecio(datos.precio());
        producto.setStock(datos.stock());
        producto.setDescripcion(datos.descripcion().trim());
        // Si no se indica imagen, se usa la convención /imagenes/productos/CODIGO.jpg
        String imagen = datos.imagen() == null || datos.imagen().isBlank()
                ? "/imagenes/productos/" + producto.getCodigo() + ".jpg"
                : datos.imagen().trim();
        producto.setImagen(imagen);
    }

    private Producto obtener(String codigo) {
        return productos.findById(codigo)
                .orElseThrow(() -> new ErrorNegocio(HttpStatus.NOT_FOUND, "El producto " + codigo + " no existe."));
    }
}
