-- Categorías del catálogo.
CREATE TABLE categoria (
    id     VARCHAR(30) NOT NULL, -- Ej: guitarras-electricas
    nombre VARCHAR(60) NOT NULL,
    PRIMARY KEY (id)
);

-- Productos. El código del Excel (GA001, GE001...) es la clave primaria y el nombre de la imagen.
CREATE TABLE producto (
    codigo       VARCHAR(5)   NOT NULL,
    categoria_id VARCHAR(30)  NOT NULL,
    nombre       VARCHAR(120) NOT NULL,
    marca        VARCHAR(60)  NOT NULL,
    modelo       VARCHAR(60)  NOT NULL,
    precio       INT          NOT NULL, -- Pesos chilenos (CLP), sin decimales
    stock        INT          NOT NULL,
    descripcion  TEXT         NOT NULL,
    imagen       VARCHAR(255) NOT NULL, -- Ruta dentro de /public del frontend
    PRIMARY KEY (codigo),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT ck_producto_precio CHECK (precio > 0),
    CONSTRAINT ck_producto_stock CHECK (stock >= 0)
);
