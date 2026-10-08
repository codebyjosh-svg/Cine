-- DML: DATOS DE PRUEBA
SET NAMES utf8mb4;
USE cinedb_in4cm;

START TRANSACTION;

INSERT INTO roles (id_rol, nombre_rol, descripcion) VALUES
    (1, 'admin', 'Administración general.'),
    (2, 'taquillero', 'Venta de boletos y productos.'),
    (3, 'bodega', 'Inventario y productos.'),
    (4, 'cliente', 'Consulta de cartelera.');

INSERT INTO clientes (id_cliente, cui, nombre_cliente, apellido_cliente, correo_electronico, telefono, estado) VALUES
    (1, '2000100010101', 'Ana', 'López', 'cliente01@example.com', '50000001', 1),
    (2, '2000100020101', 'Carlos', 'Méndez', 'cliente02@example.com', '50000002', 1),
    (3, '2000100030101', 'Luis', 'Pérez', 'cliente03@example.com', '50000003', 1),
    (4, '2000100040101', 'María', 'García', 'cliente04@example.com', '50000004', 1),
    (5, '2000100050101', 'Jorge', 'Castillo', 'cliente05@example.com', '50000005', 1),
    (6, '2000100060101', 'Lucía', 'Fernández', 'cliente06@example.com', '50000006', 1);

INSERT INTO usuarios (id_usuario, nombre_usuario, apellido_usuario, username, correo_electronico, contrasena_hash, id_rol, id_cliente) VALUES
    (1, 'Andrea', 'Morales', 'admin', 'admin@example.com', 'pbkdf2_sha256$600000$3j9IgnoEL441xuJ8e8/XKA==$0/olZKm2mBiM4GO+MAYPJmisFti3OD6zQDFZ4zk8Q8o=', 1, NULL),
    (2, 'Carlos', 'Mejía', 'taquilla', 'taquilla@example.com', 'pbkdf2_sha256$600000$c/vzksi/aqaygFrUecX57Q==$guFp/sDIwdK07Msk8opBTP8aC3dZBIVq++QtQ2Z+/O4=', 2, NULL),
    (3, 'Luisa', 'Ortiz', 'bodega', 'bodega@example.com', 'pbkdf2_sha256$600000$h5qJA75dPT6PfGQ7G6WJXw==$AFYYhy502kcdUT70ohMX3Cuq7S228cjPfIgNqTUk93I=', 3, NULL),
    (4, 'Lucía', 'Fernández', 'cliente', 'cliente06@example.com', 'pbkdf2_sha256$600000$V2CtsSpzoaP0XV0r1f/bvA==$LP34imyd0Iq+n+Np/UTxiwWm+8bNZ3ADhXTivEStvx0=', 4, 6);

INSERT INTO generos (id_genero, nombre_genero, descripcion) VALUES
    (1, 'Acción', 'Acción y aventuras.'),
    (2, 'Comedia', 'Humor.'),
    (3, 'Drama', 'Drama.'),
    (4, 'Animación', 'Animación.');

INSERT INTO peliculas (id_pelicula, titulo, sinopsis, director, duracion_minutos, clasificacion, idioma, fecha_estreno, id_genero, estado) VALUES
    (1, 'La última misión', 'Misión de rescate.', 'Daniel Ríos', 100, '12+', 'Español', '2026-09-01', 1, 1),
    (2, 'Un día de locos', 'Comedia de enredos.', 'Laura Montes', 95, 'Todo público', 'Español', '2026-09-05', 2, 1);

INSERT INTO salas (id_sala, nombre_sala, formato) VALUES
    (1, 'Sala 1', '2D'),
    (2, 'Sala 2', '2D');

INSERT INTO butacas (id_sala, fila, numero) VALUES
    (1, 'A', 1), (1, 'A', 2), (1, 'A', 3), (1, 'A', 4),
    (2, 'A', 1), (2, 'A', 2), (2, 'A', 3), (2, 'A', 4);

INSERT INTO categorias_producto (id_categoria_producto, nombre_categoria, descripcion) VALUES
    (1, 'Palomitas', 'Palomitas de maíz.'),
    (2, 'Bebidas', 'Refrescos y agua.');

INSERT INTO productos (id_producto, nombre_producto, descripcion, precio, stock_minimo, id_categoria_producto) VALUES
    (1, 'Palomitas pequeñas', 'Individual', 25.0, 20, 1),
    (2, 'Refresco mediano', 'Vaso mediano', 15.0, 20, 2);

COMMIT;

CALL sp_registrarmovimientoinventario(1, 3, 'entrada', 100, 'Stock inicial');
CALL sp_registrarmovimientoinventario(2, 3, 'entrada', 100, 'Stock inicial');

SET @dia_demo = DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY);
CALL sp_insertarfuncion(1, 1, TIMESTAMP(@dia_demo, '14:00:00'), 35.00);
CALL sp_insertarfuncion(2, 2, TIMESTAMP(@dia_demo, '16:00:00'), 40.00);