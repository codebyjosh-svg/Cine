
SET NAMES utf8mb4;
USE cinedb_in4cm;

START TRANSACTION;

INSERT INTO roles (id_rol, nombre_rol, descripcion) VALUES
    (1, 'admin', 'Administración de usuarios, catálogos, funciones y reportes.'),
    (2, 'taquillero', 'Registro de clientes, venta de boletos y confitería.'),
    (3, 'bodega', 'Control de productos y movimientos de inventario de confitería.'),
    (4, 'cliente', 'Consulta de cartelera y de sus compras desde la aplicación.');

INSERT INTO clientes (id_cliente, cui, nombre_cliente, apellido_cliente, correo_electronico, telefono, estado) VALUES
    (1, '2000100010101', 'Ana', 'López', 'cliente01@example.com', '50000001', 1),
    (2, '2000100020101', 'Carlos', 'Méndez', 'cliente02@example.com', '50000002', 1),
    (3, '2000100030101', 'Luis', 'Pérez', 'cliente03@example.com', '50000003', 1),
    (4, '2000100040101', 'María', 'García', 'cliente04@example.com', '50000004', 1),
    (5, '2000100050101', 'Jorge', 'Castillo', 'cliente05@example.com', '50000005', 1),
    (6, '2000100060101', 'Lucía', 'Fernández', 'cliente06@example.com', '50000006', 1),
    (7, '2000100070101', 'Mario', 'Gómez', 'cliente07@example.com', '50000007', 1),
    (8, '2000100080101', 'Elena', 'Morales', 'cliente08@example.com', '50000008', 1),
    (9, '2000100090101', 'Pedro', 'Ramírez', 'cliente09@example.com', '50000009', 1),
    (10, '2000100100101', 'Sofía', 'Vásquez', 'cliente10@example.com', '50000010', 1),
    (11, NULL, 'Diego', 'Hernández', 'cliente11@example.com', '50000011', 1),
    (12, NULL, 'Camila', 'Cruz', 'cliente12@example.com', '50000012', 0);

INSERT INTO usuarios (id_usuario, nombre_usuario, apellido_usuario, username, correo_electronico, contrasena_hash, id_rol, id_cliente) VALUES
    (1, 'Andrea', 'Morales', 'admin.cine', 'admin.cine@example.com', 'pbkdf2_sha256$600000$AiKMiHY7ai32oCFgL51UzA==$/hRH2SPMrMNuXnULrVM5aP1eTmFjV9wGMcqKFr3YYDs=', 1, NULL),
    (2, 'Carlos', 'Mejía', 'taquilla.cine', 'taquilla.cine@example.com', 'pbkdf2_sha256$600000$+LZFZBeyJVwB87LJSpwcQg==$lWGb6i5bpxtsQ5bJ6BwWcvO3ZT/XT1hFR+o7IUBFFDc=', 2, NULL),
    (3, 'Luisa', 'Ortiz', 'bodega.cine', 'bodega.cine@example.com', 'pbkdf2_sha256$600000$CTrBMsBCJBIqpwrIO6R44g==$b+KCJPdok3jxd+aVATUJycDXtnPxtAfgi4PbaKUSABs=', 3, NULL),
    (4, 'Lucía', 'Fernández', 'cliente.cine', 'cliente06@example.com', 'pbkdf2_sha256$600000$SEy65Yg2WuzSwr7cek637A==$IHiQmJtUBLDT/pqZEoBAk8saCxyloxu3Lp0Ec0jL/Y4=', 4, 6);

INSERT INTO generos (id_genero, nombre_genero, descripcion) VALUES
    (1, 'Acción', 'Historias con aventuras y secuencias de acción.'),
    (2, 'Comedia', 'Historias con situaciones humorísticas.'),
    (3, 'Drama', 'Historias sobre conflictos personales y familiares.'),
    (4, 'Animación', 'Historias realizadas mediante técnicas de animación.'),
    (5, 'Terror', 'Historias de suspenso y miedo.'),
    (6, 'Aventura', 'Viajes, búsquedas y descubrimientos.'),
    (7, 'Ciencia ficción', 'Historias sobre tecnología y mundos imaginarios.'),
    (8, 'Romance', 'Historias de relaciones sentimentales.'),
    (9, 'Documental', 'Relatos informativos sobre un tema.'),
    (10, 'Fantasía', 'Historias con elementos mágicos.');

INSERT INTO peliculas (id_pelicula, titulo, sinopsis, director, duracion_minutos, clasificacion, idioma, fecha_estreno, id_genero, estado) VALUES
    (1, 'La última misión', 'Un equipo intenta recuperar un dispositivo perdido.', 'Daniel Ríos', 100, '12+', 'Español', '2026-09-01', 1, 1),
    (2, 'Un día de locos', 'Dos amigos confunden sus maletas durante un viaje.', 'Laura Montes', 95, 'Todo público', 'Español', '2026-09-05', 2, 1),
    (3, 'Amigos del bosque', 'Un grupo de animales busca un nuevo hogar.', 'Pablo Luna', 90, 'Todo público', 'Español', '2026-09-08', 4, 1),
    (4, 'El faro escondido', 'Una familia descubre un secreto en la costa.', 'Elena Solís', 110, '12+', 'Español', '2026-09-10', 3, 1),
    (5, 'La casa del silencio', 'Un visitante escucha sonidos en una casa abandonada.', 'Óscar Vega', 105, '15+', 'Español', '2026-09-12', 5, 1),
    (6, 'Ruta a las estrellas', 'Un grupo de estudiantes participa en una misión espacial.', 'Marina Cruz', 120, '12+', 'Español', '2026-09-15', 7, 1),
    (7, 'El mapa perdido', 'Dos hermanos siguen las pistas de un antiguo mapa.', 'Tomás Lara', 100, 'Todo público', 'Español', '2026-09-18', 6, 1),
    (8, 'Puertas del mañana', 'Una inventora construye una máquina para observar el futuro.', 'Sara Peña', 115, '12+', 'Español', '2026-09-20', 7, 0);

INSERT INTO salas (id_sala, nombre_sala, formato) VALUES
    (1, 'Sala 1', '2D'),
    (2, 'Sala 2', '2D'),
    (3, 'Sala 3', '3D');

INSERT INTO butacas (id_sala, fila, numero) VALUES
    (1, 'A', 1),
    (1, 'A', 2),
    (1, 'A', 3),
    (1, 'A', 4),
    (1, 'A', 5),
    (1, 'A', 6),
    (1, 'B', 1),
    (1, 'B', 2),
    (1, 'B', 3),
    (1, 'B', 4),
    (1, 'B', 5),
    (1, 'B', 6),
    (1, 'C', 1),
    (1, 'C', 2),
    (1, 'C', 3),
    (1, 'C', 4),
    (1, 'C', 5),
    (1, 'C', 6),
    (1, 'D', 1),
    (1, 'D', 2),
    (1, 'D', 3),
    (1, 'D', 4),
    (1, 'D', 5),
    (1, 'D', 6),
    (2, 'A', 1),
    (2, 'A', 2),
    (2, 'A', 3),
    (2, 'A', 4),
    (2, 'A', 5),
    (2, 'A', 6),
    (2, 'B', 1),
    (2, 'B', 2),
    (2, 'B', 3),
    (2, 'B', 4),
    (2, 'B', 5),
    (2, 'B', 6),
    (2, 'C', 1),
    (2, 'C', 2),
    (2, 'C', 3),
    (2, 'C', 4),
    (2, 'C', 5),
    (2, 'C', 6),
    (2, 'D', 1),
    (2, 'D', 2),
    (2, 'D', 3),
    (2, 'D', 4),
    (2, 'D', 5),
    (2, 'D', 6),
    (3, 'A', 1),
    (3, 'A', 2),
    (3, 'A', 3),
    (3, 'A', 4),
    (3, 'A', 5),
    (3, 'A', 6),
    (3, 'B', 1),
    (3, 'B', 2),
    (3, 'B', 3),
    (3, 'B', 4),
    (3, 'B', 5),
    (3, 'B', 6),
    (3, 'C', 1),
    (3, 'C', 2),
    (3, 'C', 3),
    (3, 'C', 4),
    (3, 'C', 5),
    (3, 'C', 6),
    (3, 'D', 1),
    (3, 'D', 2),
    (3, 'D', 3),
    (3, 'D', 4),
    (3, 'D', 5),
    (3, 'D', 6);

UPDATE butacas SET estado = 0 WHERE id_sala = 3 AND fila = 'D' AND numero = 6;

INSERT INTO categorias_producto (id_categoria_producto, nombre_categoria, descripcion) VALUES
    (1, 'Palomitas', 'Palomitas de maíz en diferentes tamaños.'),
    (2, 'Bebidas', 'Agua y refrescos.'),
    (3, 'Comida', 'Nachos y alimentos preparados.'),
    (4, 'Dulces', 'Chocolates y golosinas.');

INSERT INTO productos (id_producto, nombre_producto, descripcion, precio, stock_minimo, id_categoria_producto) VALUES
    (1, 'Palomitas pequeñas', 'Porción individual.', 25.0, 20, 1),
    (2, 'Palomitas grandes', 'Porción grande.', 35.0, 20, 1),
    (3, 'Agua pura', 'Botella de agua.', 12.0, 10, 2),
    (4, 'Refresco mediano', 'Vaso mediano de refresco.', 15.0, 20, 2),
    (5, 'Nachos', 'Porción con salsa de queso.', 30.0, 15, 3),
    (6, 'Hot dog', 'Pan con salchicha.', 25.0, 10, 3),
    (7, 'Chocolate', 'Barra de chocolate.', 18.0, 15, 4),
    (8, 'Bolsa de dulces', 'Bolsa individual de golosinas.', 10.0, 10, 4);

COMMIT;

CALL sp_registrarmovimientoinventario(1, 3, 'entrada', 120, 'Carga inicial de demostración');
CALL sp_registrarmovimientoinventario(2, 3, 'entrada', 100, 'Carga inicial de demostración');
CALL sp_registrarmovimientoinventario(3, 3, 'entrada', 60, 'Carga inicial de demostración');
CALL sp_registrarmovimientoinventario(4, 3, 'entrada', 150, 'Carga inicial de demostración');
CALL sp_registrarmovimientoinventario(5, 3, 'entrada', 80, 'Carga inicial de demostración');
CALL sp_registrarmovimientoinventario(6, 3, 'entrada', 40, 'Carga inicial de demostración');
CALL sp_registrarmovimientoinventario(7, 3, 'entrada', 75, 'Carga inicial de demostración');
CALL sp_registrarmovimientoinventario(8, 3, 'entrada', 8, 'Carga inicial de demostración');

SET @dia_demo = DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY);
SET @dia_siguiente = DATE_ADD(@dia_demo, INTERVAL 1 DAY);
CALL sp_insertarfuncion(1, 1, TIMESTAMP(@dia_demo, '14:00:00'), 35.00);
SET @funcion1 = LAST_INSERT_ID();
CALL sp_insertarfuncion(2, 2, TIMESTAMP(@dia_demo, '14:00:00'), 40.00);
SET @funcion2 = LAST_INSERT_ID();
CALL sp_insertarfuncion(3, 3, TIMESTAMP(@dia_demo, '14:00:00'), 45.00);
SET @funcion3 = LAST_INSERT_ID();
CALL sp_insertarfuncion(4, 1, TIMESTAMP(@dia_demo, '18:00:00'), 35.00);
SET @funcion4 = LAST_INSERT_ID();
CALL sp_insertarfuncion(5, 2, TIMESTAMP(@dia_demo, '18:00:00'), 40.00);
SET @funcion5 = LAST_INSERT_ID();
CALL sp_insertarfuncion(6, 3, TIMESTAMP(@dia_demo, '18:00:00'), 45.00);
SET @funcion6 = LAST_INSERT_ID();
CALL sp_insertarfuncion(7, 1, TIMESTAMP(@dia_siguiente, '14:00:00'), 35.00);
SET @funcion7 = LAST_INSERT_ID();
CALL sp_insertarfuncion(4, 2, TIMESTAMP(@dia_siguiente, '14:00:00'), 40.00);
SET @funcion8 = LAST_INSERT_ID();
CALL sp_insertarfuncion(3, 3, TIMESTAMP(@dia_siguiente, '14:00:00'), 45.00);
SET @funcion9 = LAST_INSERT_ID();

SET @s1a1 = (SELECT id_butaca FROM butacas WHERE id_sala = 1 AND fila = 'A' AND numero = 1);
SET @s1a2 = (SELECT id_butaca FROM butacas WHERE id_sala = 1 AND fila = 'A' AND numero = 2);
SET @s1a3 = (SELECT id_butaca FROM butacas WHERE id_sala = 1 AND fila = 'A' AND numero = 3);
SET @s2a1 = (SELECT id_butaca FROM butacas WHERE id_sala = 2 AND fila = 'A' AND numero = 1);
SET @s3a1 = (SELECT id_butaca FROM butacas WHERE id_sala = 3 AND fila = 'A' AND numero = 1);
SET @s3a2 = (SELECT id_butaca FROM butacas WHERE id_sala = 3 AND fila = 'A' AND numero = 2);

CALL sp_abrirventa(1, 2);
SET @venta1 = LAST_INSERT_ID();
CALL sp_agregarboleto(@venta1, @funcion1, @s1a1);
CALL sp_agregarboleto(@venta1, @funcion1, @s1a2);
CALL sp_agregarproductoventa(@venta1, 1, 1);
CALL sp_agregarproductoventa(@venta1, 1, 1); -- incrementa la cantidad del mismo producto a 2.
CALL sp_agregarproductoventa(@venta1, 4, 2);
CALL sp_confirmarventa(@venta1);

CALL sp_abrirventa(2, 2);
SET @venta2 = LAST_INSERT_ID();
CALL sp_agregarboleto(@venta2, @funcion2, @s2a1);
CALL sp_agregarproductoventa(@venta2, 2, 1);
CALL sp_agregarproductoventa(@venta2, 4, 1);
CALL sp_confirmarventa(@venta2);

CALL sp_abrirventa(3, 2);
SET @venta3 = LAST_INSERT_ID();
CALL sp_agregarboleto(@venta3, @funcion3, @s3a1);
CALL sp_agregarboleto(@venta3, @funcion3, @s3a2);
CALL sp_agregarproductoventa(@venta3, 3, 2);
CALL sp_confirmarventa(@venta3);

CALL sp_abrirventa(4, 2);
SET @venta4 = LAST_INSERT_ID();
CALL sp_agregarboleto(@venta4, @funcion4, @s1a3);
CALL sp_agregarproductoventa(@venta4, 8, 3);
CALL sp_confirmarventa(@venta4);
CALL sp_anularventa(@venta4, 1, 'Anulación de demostración solicitada por el cliente');

CALL sp_abrirventa(5, 2);
SET @venta5 = LAST_INSERT_ID();
CALL sp_agregarboleto(@venta5, @funcion4, @s1a3);
CALL sp_agregarproductoventa(@venta5, 1, 1);
CALL sp_confirmarventa(@venta5);

SELECT * FROM vw_lista_clientes;
SELECT * FROM vw_lista_peliculas;
SELECT * FROM vw_lista_salas;
SELECT * FROM vw_cartelera ORDER BY fecha_inicio, id_sala;
SELECT * FROM vw_lista_ventas ORDER BY id_venta;
SELECT * FROM vw_stock_critico;
CALL sp_verfactura(@venta1);
CALL sp_reporteventasdia(CURRENT_DATE);
CALL sp_reporteventassemana(CURRENT_DATE);
CALL sp_reporteventasmes(CURRENT_DATE);
CALL sp_indicadoresadmin();

