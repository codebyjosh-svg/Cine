-- ============================================================
-- 02_DML_Cine.sql
-- Datos iniciales y datos de prueba
-- ============================================================

SET NAMES utf8mb4;
USE cinedb_in4cm;

START TRANSACTION;

-- ============================================================
-- 1. ROLES
-- ============================================================

INSERT INTO roles (id_rol, nombre_rol, descripcion) VALUES
(1, 'admin', 'Administracion general del cine.'),
(2, 'taquillero', 'Venta de boletos y atencion al cliente.'),
(3, 'bodega', 'Control de inventario y productos de dulceria.'),
(4, 'cliente', 'Consulta de cartelera y compras.');

-- ============================================================
-- 2. CLIENTES
-- ============================================================

INSERT INTO clientes
(id_cliente, cui, nombre_cliente, apellido_cliente, nit,
 correo_electronico, telefono, estado)
VALUES
(1, '3001000010101', 'Ana', 'Lopez', 'CF',
 'ana.lopez@cine.com', '55550001', 1),
(2, '3001000020101', 'Carlos', 'Mendez', '1234567-8',
 'carlos.mendez@cine.com', '55550002', 1),
(3, '3001000030101', 'Luis', 'Perez', '8765432-1',
 'luis.perez@cine.com', '55550003', 1),
(4, '3001000040101', 'Maria', 'Garcia', '2345678-9',
 'maria.garcia@cine.com', '55550004', 1),
(5, '3001000050101', 'Jorge', 'Castillo', '3456789-0',
 'jorge.castillo@cine.com', '55550005', 1),
(6, '3001000060101', 'Sofia', 'Morales', '4567890-1',
 'sofia.morales@cine.com', '55550006', 1),
(7, '3001000070101', 'Diego', 'Rios', '5678901-2',
 'diego.rios@cine.com', '55550007', 1),
(8, '3001000080101', 'Elena', 'Gomez', '6789012-3',
 'elena.gomez@cine.com', '55550008', 1),
(9, '3001000090101', 'Pedro', 'Ramirez', '7890123-4',
 'pedro.ramirez@cine.com', '55550009', 1),
(10, '3001000100101', 'Laura', 'Martinez', '8901234-5',
 'laura.martinez@cine.com', '55550010', 1);

-- ============================================================
-- 3. USUARIOS
-- Contraseña de los cuatro usuarios: 12345678
-- Formato PBKDF2-HMAC-SHA256, 600000 iteraciones.
-- ============================================================

INSERT INTO usuarios
(id_usuario, nombre_usuario, apellido_usuario, username,
 correo_electronico, contrasena_hash, id_rol, id_cliente, estado)
VALUES
(1, 'Admin', 'Sistema', 'admin',
 'admin@cine.com',
 'pbkdf2_sha256$600000$Hw60QzgU7uipZAAahohm8A==$k0zXZutl5WUHrT+p6Hh5i2NaB9caWIs1IkZRBUrIEwc=', 1, NULL, 1),

(2, 'Pedro', 'Taquillero', 'taquilla',
 'taquilla@cine.com',
 'pbkdf2_sha256$600000$v19g9dFlYvtim+tZtuTDCA==$ppIZOKJuVVYedhWmJjA1JGoteUIHRwShjcJmkTALuHU=', 2, NULL, 1),

(3, 'Marta', 'Bodega', 'bodega',
 'bodega@cine.com',
 'pbkdf2_sha256$600000$vKLqev8Y+nfeqTM3x4fmnw==$HhtAWWdjTmYjetAZa0U0GTVsqph/uYTucxUCQrZZ4vU=', 3, NULL, 1),

(4, 'Ana', 'Lopez', 'cliente',
 'cliente@cine.com',
 'pbkdf2_sha256$600000$x1luRoIC3nJAEuzwbz77fQ==$7upxcyafEaYqzUOIQE6sReImHba+y2ekFGnqB+97Gj8=', 4, 1, 1);

-- ============================================================
-- 4. GENEROS
-- ============================================================

INSERT INTO generos
(id_genero, nombre_genero, descripcion, estado)
VALUES
(1, 'Accion', 'Peliculas de accion y aventura.', 1),
(2, 'Comedia', 'Peliculas de humor y entretenimiento.', 1),
(3, 'Drama', 'Historias dramaticas y emocionales.', 1),
(4, 'Animacion', 'Peliculas animadas.', 1),
(5, 'Terror', 'Peliculas de terror y suspenso.', 1),
(6, 'Ciencia Ficcion', 'Historias futuristas y de ciencia.', 1),
(7, 'Aventura', 'Historias de viajes y descubrimiento.', 1),
(8, 'Romance', 'Historias romanticas.', 1);

-- ============================================================
-- 5. PELICULAS
-- La imagen queda NULL y se puede cargar desde JavaFX.
-- ============================================================

INSERT INTO peliculas
(id_pelicula, titulo, sinopsis, director, duracion_minutos,
 clasificacion, idioma, fecha_estreno, imagen, id_genero, estado)
VALUES
(1, 'La ultima mision',
 'Un equipo de rescate debe completar una mision antes del amanecer.',
 'Daniel Rios', 100, '12+', 'Español', '2026-09-01', NULL, 1, 1),

(2, 'Un dia de locos',
 'Una familia vive un dia lleno de situaciones inesperadas.',
 'Laura Montes', 95, 'Todo público', 'Español', '2026-09-05', NULL, 2, 1),

(3, 'Viaje al espacio',
 'Una tripulacion emprende un viaje para descubrir un nuevo planeta.',
 'Carlos Ruiz', 130, '7+', 'Español', '2026-09-10', NULL, 6, 1),

(4, 'La gran historia',
 'Un joven intenta superar los problemas de su pasado.',
 'Maria Torres', 115, '12+', 'Español', '2026-09-12', NULL, 3, 1),

(5, 'Mundo de colores',
 'Una aventura animada para toda la familia.',
 'Pedro Lopez', 90, 'Todo público', 'Español', '2026-09-15', NULL, 4, 1),

(6, 'La casa oscura',
 'Un grupo de amigos descubre un misterio en una casa abandonada.',
 'Luis Gomez', 105, '15+', 'Español', '2026-09-18', NULL, 5, 1),

(7, 'La isla perdida',
 'Una expedicion busca una isla que aparece en antiguos mapas.',
 'Jorge Castillo', 120, '7+', 'Español', '2026-09-20', NULL, 7, 1),

(8, 'Siempre contigo',
 'Dos personas descubren que sus caminos estaban destinados a encontrarse.',
 'Sofia Morales', 110, '12+', 'Español', '2026-09-22', NULL, 8, 1),

(9, 'Rescate extremo',
 'Un grupo de especialistas enfrenta una operacion de alto riesgo.',
 'Diego Rios', 125, '18+', 'Español', '2026-09-25', NULL, 1, 1),

(10, 'Noche de misterio',
 'Un detective intenta resolver una serie de sucesos extraños.',
 'Elena Gomez', 108, '15+', 'Español', '2026-09-28', NULL, 5, 1);

-- ============================================================
-- 6. SALAS
-- ============================================================

INSERT INTO salas
(id_sala, nombre_sala, formato, estado)
VALUES
(1, 'Sala 1', '2D', 1),
(2, 'Sala 2', '2D', 1),
(3, 'Sala 3', '3D', 1),
(4, 'Sala 4', '2D', 1);

-- ============================================================
-- 7. BUTACAS
-- ============================================================

INSERT INTO butacas
(id_butaca, id_sala, fila, numero, estado)
VALUES
(1,1,'A',1,1),(2,1,'A',2,1),(3,1,'A',3,1),(4,1,'A',4,1),
(5,1,'B',1,1),(6,1,'B',2,1),(7,1,'B',3,1),(8,1,'B',4,1),

(9,2,'A',1,1),(10,2,'A',2,1),(11,2,'A',3,1),(12,2,'A',4,1),
(13,2,'B',1,1),(14,2,'B',2,1),(15,2,'B',3,1),(16,2,'B',4,1),

(17,3,'A',1,1),(18,3,'A',2,1),(19,3,'A',3,1),(20,3,'A',4,1),
(21,3,'B',1,1),(22,3,'B',2,1),(23,3,'B',3,1),(24,3,'B',4,1),

(25,4,'A',1,1),(26,4,'A',2,1),(27,4,'A',3,1),(28,4,'A',4,1),
(29,4,'B',1,1),(30,4,'B',2,1),(31,4,'B',3,1),(32,4,'B',4,1);

-- ============================================================
-- 8. FUNCIONES
-- Las funciones 1 y 2 quedan canceladas para poder probar
-- facilmente Activar/Desactivar peliculas.
-- ============================================================

INSERT INTO funciones
(id_funcion, id_pelicula, id_sala, fecha_inicio, fecha_fin,
 precio_boleto, estado)
VALUES
(1, 1, 1,
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '14:00:00'),
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '15:40:00'),
 45.00, 'cancelada'),

(2, 2, 1,
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '17:00:00'),
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '18:35:00'),
 45.00, 'cancelada'),

(3, 3, 2,
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '15:00:00'),
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '17:10:00'),
 55.00, 'programada'),

(4, 4, 3,
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '11:00:00'),
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '12:55:00'),
 40.00, 'programada'),

(5, 5, 4,
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '18:00:00'),
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '19:30:00'),
 50.00, 'programada'),

(6, 6, 2,
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), '20:00:00'),
 TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), '21:45:00'),
 50.00, 'programada');

-- ============================================================
-- 9. CATEGORIAS DE PRODUCTO
-- ============================================================

INSERT INTO categorias_producto
(id_categoria_producto, nombre_categoria, descripcion, estado)
VALUES
(1, 'Combos', 'Combos de comida y bebida.', 1),
(2, 'Poporopos', 'Poporopos de diferentes tamaños y sabores.', 1),
(3, 'Bebidas', 'Gaseosas, agua y otras bebidas.', 1),
(4, 'Dulces', 'Chocolates y golosinas.', 1),
(5, 'Comida', 'Nachos y comida rapida.', 1);

-- ============================================================
-- 10. PRODUCTOS
-- Algunos productos tienen stock critico.
-- ============================================================

INSERT INTO productos
(id_producto, id_categoria_producto, nombre_producto, descripcion,
 precio, stock, stock_minimo, estado)
VALUES
(1,1,'Combo Pareja','2 poporopos + 2 bebidas',85.00,50,10,1),
(2,1,'Combo Personal','1 poporopo + 1 bebida',45.00,40,10,1),
(3,2,'Poporopo Grande','Poporopo grande con mantequilla',35.00,100,15,1),
(4,2,'Poporopo Caramelo','Poporopo acaramelado',40.00,3,15,1),
(5,3,'Gaseosa Grande','Gaseosa de 32 oz',20.00,200,30,1),
(6,3,'Agua 600ml','Agua pura',12.00,4,20,1),
(7,4,'Chocolate','Chocolate individual',18.00,2,10,1),
(8,5,'Nachos','Nachos con queso',28.00,25,8,1),
(9,5,'Hot Dog','Hot dog con aderezos',25.00,18,5,1),
(10,3,'Te Frio','Te frio de melocoton',15.00,30,10,1),
(11,4,'Gomitas','Bolsa de gomitas',12.00,45,10,1),
(12,1,'Combo Familiar','2 poporopos + 4 bebidas',150.00,12,5,1),
(13,2,'Poporopo Mixto','Poporopo de sabores',38.00,1,10,1),
(14,3,'Gaseosa Naranja','Gaseosa grande',20.00,80,15,1),
(15,4,'Barra de Chocolate','Barra de chocolate',15.00,50,10,1);

-- ============================================================
-- 11. VENTAS
-- ============================================================

INSERT INTO ventas
(id_venta, fecha_venta, id_cliente, id_usuario, total,
 metodo_pago, estado, fecha_confirmacion)
VALUES
(1, NOW() - INTERVAL 2 DAY, 2, 2, 163.00,
 'EFECTIVO', 'confirmada', NOW() - INTERVAL 2 DAY),

(2, NOW() - INTERVAL 1 DAY, 3, 2, 65.00,
 'TARJETA', 'confirmada', NOW() - INTERVAL 1 DAY),

(3, NOW() - INTERVAL 3 HOUR, 4, 2, 67.00,
 'EFECTIVO', 'confirmada', NOW() - INTERVAL 3 HOUR);

-- ============================================================
-- 12. BOLETOS
-- ============================================================

INSERT INTO boletos
(id_boleto, id_venta, id_funcion, id_butaca, id_sala,
 precio_unitario, estado)
VALUES
(1,1,1,1,1,45.00,'vendido'),
(2,1,1,2,1,45.00,'vendido'),
(3,2,2,9,2,45.00,'vendido'),
(4,3,3,10,2,55.00,'vendido');

-- ============================================================
-- 13. DETALLE DE VENTA DE PRODUCTOS
-- ============================================================

INSERT INTO detalle_venta_productos
(id_detalle_producto, id_venta, id_producto, cantidad,
 precio_unitario, subtotal)
VALUES
(1,1,2,1,45.00,45.00),
(2,1,8,1,28.00,28.00),
(3,2,5,1,20.00,20.00),
(4,3,6,1,12.00,12.00);

-- ============================================================
-- 14. MOVIMIENTOS DE INVENTARIO
-- ============================================================

INSERT INTO movimientos_inventario
(id_movimiento, id_producto, id_usuario, id_venta,
 tipo_movimiento, cantidad, fecha_movimiento, observacion)
VALUES
(1,2,3,NULL,'ENTRADA',50,NOW()-INTERVAL 5 DAY,'Carga inicial.'),
(2,2,3,NULL,'SALIDA',10,NOW()-INTERVAL 4 DAY,'Ajuste de salida de inventario.'),
(3,8,3,NULL,'ENTRADA',25,NOW()-INTERVAL 4 DAY,'Carga inicial.'),
(4,5,3,NULL,'ENTRADA',201,NOW()-INTERVAL 4 DAY,'Carga inicial.'),
(5,5,2,2,'SALIDA',1,NOW()-INTERVAL 1 DAY,'Salida por venta.'),
(6,6,3,NULL,'ENTRADA',4,NOW()-INTERVAL 3 DAY,'Carga inicial.'),
(7,7,3,NULL,'ENTRADA',2,NOW()-INTERVAL 3 DAY,'Carga inicial.');

COMMIT;

-- ============================================================
-- VALIDACIONES DML
-- ============================================================

SELECT COUNT(*) AS total_roles FROM roles;
SELECT COUNT(*) AS total_clientes FROM clientes;
SELECT COUNT(*) AS total_usuarios FROM usuarios;
SELECT COUNT(*) AS total_generos FROM generos;
SELECT COUNT(*) AS total_peliculas FROM peliculas;
SELECT COUNT(*) AS total_salas FROM salas;
SELECT COUNT(*) AS total_butacas FROM butacas;
SELECT COUNT(*) AS total_funciones FROM funciones;
SELECT COUNT(*) AS total_ventas FROM ventas;
SELECT COUNT(*) AS total_boletos FROM boletos;
SELECT COUNT(*) AS total_categorias FROM categorias_producto;
SELECT COUNT(*) AS total_productos FROM productos;
SELECT COUNT(*) AS total_detalles_productos FROM detalle_venta_productos;
SELECT COUNT(*) AS total_movimientos FROM movimientos_inventario;
