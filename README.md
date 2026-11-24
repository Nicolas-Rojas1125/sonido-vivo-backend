# Sonido Vivo – Backend (Spring Boot 4 + MySQL + Flyway + JWT)

| Microservicio | Puerto | Base de datos | Swagger | Responsabilidad |
|---|---|---|---|---|
| `ms-usuarios` | 8081 | `sonidovivo_usuarios` | http://localhost:8081/swagger-ui.html | Login JWT (access + refresh token rotativo en cookie httpOnly), registro, usuarios y roles |
| `ms-contactos` | 8082 | `sonidovivo_contactos` | http://localhost:8082/swagger-ui.html | Mensajes del formulario de contacto |
| `ms-tienda` | 8083 | `sonidovivo_tienda` | http://localhost:8083/swagger-ui.html | Catálogo, inventario y pedidos (stock atómico, precios del servidor) |

- Requisitos: JDK 17+, MySQL 8 (Laragon, `root` sin contraseña). Las bases se crean solas y Flyway aplica las migraciones de `src/main/resources/db/migracion`.
- Ejecutar: abrir esta carpeta en VS Code → Spring Boot Dashboard → ▶ (primero `ms-usuarios`).
- Los 3 servicios comparten la clave JWT (`jwt.secreto`, o la variable de entorno `JWT_SECRETO` en producción).
- Pruebas de integración: `src/test/java` (JUnit 5 + MockMvc), usan bases `*_test` y necesitan MySQL encendido.
- Postman: colección en la carpeta `postman/` de cada servicio.

Usuarios de prueba: `admin@sonidovivo.cl / Admin123` · `vendedor@sonidovivo.cl / Vende123` · `cliente@correo.cl / Cliente123`

Frontend: https://github.com/Nicolas-Rojas1125/sonido-vivo
