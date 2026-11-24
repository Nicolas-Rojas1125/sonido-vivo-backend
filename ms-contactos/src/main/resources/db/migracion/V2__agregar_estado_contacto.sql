-- Agregamos la columna estado a la tabla contacto ya existente.
-- Los contactos existentes quedarán con estado PENDIENTE.
-- Valores posibles: PENDIENTE (recién llegado) o ATENDIDO (ya se le respondió al cliente).
ALTER TABLE contacto
ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';
