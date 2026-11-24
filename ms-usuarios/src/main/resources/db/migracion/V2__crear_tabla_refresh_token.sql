-- Refresh tokens: permiten renovar el access token (JWT) sin volver a pedir la contraseña.
-- Se guarda solo el HASH SHA-256 del token: si alguien lee esta tabla, no puede usar los tokens.
CREATE TABLE refresh_token (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    token_hash     VARCHAR(64) NOT NULL,              -- SHA-256 en hexadecimal (64 caracteres)
    usuario_id     BIGINT      NOT NULL,
    expira_en      DATETIME(6) NOT NULL,
    revocado       BOOLEAN     NOT NULL DEFAULT FALSE, -- true al renovar (rotación) o al cerrar sesión
    fecha_creacion DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);
