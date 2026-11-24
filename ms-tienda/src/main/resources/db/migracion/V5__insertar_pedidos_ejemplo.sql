-- Pedidos de ejemplo del Cliente de Prueba (usuario id 3 en ms-usuarios),
-- para que el panel de administración no parta vacío. El pedido 3 es de una compra antigua
-- sin cuenta registrada (usuario_id NULL).
INSERT INTO pedido (id, usuario_id, cliente_nombre, cliente_correo, telefono, fecha, tipo_entrega, direccion, comuna, punto_retiro, medio_pago, subtotal, costo_envio, total, estado, seguimiento) VALUES
(1, 3, 'Cliente de Prueba', 'cliente@correo.cl', '+56 9 1111 2222', '2026-10-05 11:20:00', 'despacho', 'Calle Los Aromos 123', 'Concepción', '', 'webpay', 26970, 5990, 32960, 'entregado', 'STK-4455123'),
(2, 3, 'Cliente de Prueba', 'cliente@correo.cl', '+56 9 1111 2222', '2026-10-18 17:45:00', 'retiro', '', '', 'tienda', 'transferencia', 79990, 0, 79990, 'despachado', ''),
(3, NULL, 'María Pérez', 'maria.perez@correo.cl', '+56 9 3333 4444', '2026-10-25 09:10:00', 'despacho', 'Av. Alemania 456', 'Temuco', '', 'webpay', 149990, 5990, 155980, 'preparacion', '');

INSERT INTO detalle_pedido (pedido_id, producto_codigo, nombre_producto, precio_unitario, cantidad) VALUES
(1, 'AC001', 'Cuerdas Guitarra Eléctrica 09-42', 8990, 2),
(1, 'AC006', 'Afinador de Clip', 8990, 1),
(2, 'PE001', 'Pedal Distorsión', 79990, 1),
(3, 'MI001', 'Micrófono Dinámico Cardioide', 149990, 1);
