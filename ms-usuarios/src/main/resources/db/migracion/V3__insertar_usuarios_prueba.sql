-- Usuarios de prueba (uno por rol). Las contraseñas están encriptadas con BCrypt:
--   admin@sonidovivo.cl    -> Admin123
--   vendedor@sonidovivo.cl -> Vende123
--   cliente@correo.cl      -> Cliente123
INSERT INTO usuario (id, rut, nombre, correo, clave_hash, rol, activo, fecha_creacion) VALUES
(1, '11.111.111-1', 'Dueño Sonido Vivo',    'admin@sonidovivo.cl',    '$2a$10$.lRvoWnDVGSfXZP8BZtEfuPJK8/P2Zle1QCr54NITQPCIcW40kJoG', 'ADMINISTRADOR', TRUE, NOW(6)),
(2, '12.345.678-5', 'Vendedor Sonido Vivo', 'vendedor@sonidovivo.cl', '$2a$10$ZD5eV3kaFpP65ICNA.JpouqSjSCa39iZuoEissmxOY/XceT77dKTC', 'VENDEDOR',      TRUE, NOW(6)),
(3, '9.876.543-3',  'Cliente de Prueba',    'cliente@correo.cl',      '$2a$10$K9elxgQ6k80UvAYVTwm9.O8tW5SBU.YvT1KgmOEHdJfoOuaaTA7Eq', 'CLIENTE',       TRUE, NOW(6));
