-- Índice para acelerar el filtro del catálogo por categoría.
-- (Ejemplo de una segunda migración que MODIFICA la estructura existente.)
CREATE INDEX idx_producto_categoria_nombre ON producto (categoria_id, nombre);
