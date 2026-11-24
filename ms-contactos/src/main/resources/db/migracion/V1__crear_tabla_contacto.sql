-- Crea la tabla contacto: guarda los mensajes que envían los clientes desde la vista Contacto.
CREATE TABLE contacto (
    id              BIGINT       NOT NULL AUTO_INCREMENT, -- Identificador único generado automáticamente por MySQL.
    nombre_completo VARCHAR(150) NOT NULL,                -- Nombre completo de quien envía el mensaje.
    correo          VARCHAR(150) NOT NULL,                -- Correo electrónico para responderle.
    telefono        VARCHAR(20)  NULL,                    -- Teléfono (opcional en el formulario).
    asunto          VARCHAR(30)  NOT NULL,                -- stock, pedido, reparacion u otro.
    mensaje         TEXT         NOT NULL,                -- Contenido del mensaje. TEXT permite textos extensos.
    fecha_creacion  DATETIME(6)  NOT NULL,                -- Fecha y hora de creación, asignada con @PrePersist.
    PRIMARY KEY (id)                                      -- Define el identificador como clave primaria.
);
