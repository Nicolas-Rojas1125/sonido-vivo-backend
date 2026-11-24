-- Pedidos. usuario_id es el id del cliente en ms-usuarios (otra base de datos, por eso no hay FOREIGN KEY:
-- cada microservicio es dueño de sus datos). Se copian nombre y correo para el historial.
CREATE TABLE pedido (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id     BIGINT       NULL,
    cliente_nombre VARCHAR(120) NOT NULL,
    cliente_correo VARCHAR(150) NOT NULL,
    telefono       VARCHAR(20)  NOT NULL,
    fecha          DATETIME(6)  NOT NULL,
    tipo_entrega   VARCHAR(10)  NOT NULL, -- despacho | retiro
    direccion      VARCHAR(150) NOT NULL DEFAULT '',
    comuna         VARCHAR(60)  NOT NULL DEFAULT '',
    punto_retiro   VARCHAR(30)  NOT NULL DEFAULT '',
    medio_pago     VARCHAR(15)  NOT NULL, -- transferencia | webpay
    subtotal       INT          NOT NULL,
    costo_envio    INT          NOT NULL,
    total          INT          NOT NULL,
    estado         VARCHAR(15)  NOT NULL DEFAULT 'preparacion', -- preparacion | despachado | entregado
    seguimiento    VARCHAR(40)  NOT NULL DEFAULT '',            -- N° Starken / Chilexpress
    PRIMARY KEY (id)
);
CREATE INDEX idx_pedido_usuario ON pedido (usuario_id);

-- Detalle (productos de cada pedido). Se copia nombre y precio del momento de la compra:
-- si después cambia el catálogo, el pedido no cambia.
CREATE TABLE detalle_pedido (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    pedido_id       BIGINT       NOT NULL,
    producto_codigo VARCHAR(5)   NOT NULL,
    nombre_producto VARCHAR(120) NOT NULL,
    precio_unitario INT          NOT NULL,
    cantidad        INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id) ON DELETE CASCADE
);
