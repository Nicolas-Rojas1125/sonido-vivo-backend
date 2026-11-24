-- Usuarios del sistema (los 3 roles del caso: ADMINISTRADOR, VENDEDOR y CLIENTE).
CREATE TABLE usuario (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    rut            VARCHAR(12)  NOT NULL,              -- Formato 12.345.678-5 (validado con módulo 11)
    nombre         VARCHAR(120) NOT NULL,
    correo         VARCHAR(150) NOT NULL,              -- Se usa para iniciar sesión
    clave_hash     VARCHAR(100) NOT NULL,              -- Contraseña ENCRIPTADA con BCrypt (nunca en texto plano)
    rol            VARCHAR(20)  NOT NULL,              -- ADMINISTRADOR, VENDEDOR o CLIENTE
    activo         BOOLEAN      NOT NULL DEFAULT TRUE, -- Las cuentas no se borran: se desactivan
    fecha_creacion DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_correo UNIQUE (correo),
    CONSTRAINT uk_usuario_rut UNIQUE (rut)
);
