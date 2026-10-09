-- ============================================================
-- 03_SP_Vistas_Cine_Corregido_Final.sql
-- Procedimientos almacenados, vistas y compatibilidad DAO
-- Base: cinedb_in4cm
-- ============================================================

SET NAMES utf8mb4;
USE cinedb_in4cm;

-- ============================================================
-- LIMPIEZA DE PROCEDIMIENTOS
-- ============================================================

DROP PROCEDURE IF EXISTS sp_buscarusuario_login;
DROP PROCEDURE IF EXISTS sp_insertarusuario;
DROP PROCEDURE IF EXISTS sp_listarusuarios;
DROP PROCEDURE IF EXISTS sp_buscarusuario;
DROP PROCEDURE IF EXISTS sp_actualizarusuario;
DROP PROCEDURE IF EXISTS sp_eliminarusuario;
DROP PROCEDURE IF EXISTS sp_cambiarestadousuario;
DROP PROCEDURE IF EXISTS sp_cambiarcontrasena;
DROP PROCEDURE IF EXISTS sp_listarroles;

DROP PROCEDURE IF EXISTS sp_insertarcliente;
DROP PROCEDURE IF EXISTS sp_listarclientes;
DROP PROCEDURE IF EXISTS sp_buscarcliente;
DROP PROCEDURE IF EXISTS sp_actualizarcliente;
DROP PROCEDURE IF EXISTS sp_eliminarcliente;
DROP PROCEDURE IF EXISTS sp_cambiarestadocliente;
DROP PROCEDURE IF EXISTS sp_listarclientesactivos;

DROP PROCEDURE IF EXISTS sp_insertargenero;
DROP PROCEDURE IF EXISTS sp_listargeneros;
DROP PROCEDURE IF EXISTS sp_buscargenero;
DROP PROCEDURE IF EXISTS sp_actualizargenero;
DROP PROCEDURE IF EXISTS sp_eliminargenero;
DROP PROCEDURE IF EXISTS sp_cambiarestadogenero;

DROP PROCEDURE IF EXISTS sp_insertarpelicula;
DROP PROCEDURE IF EXISTS sp_listarpeliculas;
DROP PROCEDURE IF EXISTS sp_buscarpelicula;
DROP PROCEDURE IF EXISTS sp_actualizarpelicula;
DROP PROCEDURE IF EXISTS sp_eliminarpelicula;
DROP PROCEDURE IF EXISTS sp_cambiarestadopelicula;

DROP PROCEDURE IF EXISTS sp_insertarsala;
DROP PROCEDURE IF EXISTS sp_listarsalas;
DROP PROCEDURE IF EXISTS sp_buscarsala;
DROP PROCEDURE IF EXISTS sp_actualizarsala;
DROP PROCEDURE IF EXISTS sp_eliminarsala;
DROP PROCEDURE IF EXISTS sp_cambiarestadosala;

DROP PROCEDURE IF EXISTS sp_insertarbutaca;
DROP PROCEDURE IF EXISTS sp_listarbutacas;
DROP PROCEDURE IF EXISTS sp_buscarbutaca;
DROP PROCEDURE IF EXISTS sp_actualizarbutaca;
DROP PROCEDURE IF EXISTS sp_eliminarbutaca;
DROP PROCEDURE IF EXISTS sp_cambiarestadobutaca;

DROP PROCEDURE IF EXISTS sp_insertarfuncion;
DROP PROCEDURE IF EXISTS sp_listarfunciones;
DROP PROCEDURE IF EXISTS sp_buscarfuncion;
DROP PROCEDURE IF EXISTS sp_actualizarfuncion;
DROP PROCEDURE IF EXISTS sp_eliminarfuncion;
DROP PROCEDURE IF EXISTS sp_cancelarfuncion;
DROP PROCEDURE IF EXISTS sp_finalizarfuncion;
DROP PROCEDURE IF EXISTS sp_listarbutacasfuncion;
DROP PROCEDURE IF EXISTS sp_consultarbutacasdisponibles;

DROP PROCEDURE IF EXISTS sp_abrirventa;
DROP PROCEDURE IF EXISTS sp_listarventas;
DROP PROCEDURE IF EXISTS sp_buscarventa;
DROP PROCEDURE IF EXISTS sp_actualizarclienteventa;
DROP PROCEDURE IF EXISTS sp_agregarboleto;
DROP PROCEDURE IF EXISTS sp_eliminarboleto;
DROP PROCEDURE IF EXISTS sp_confirmarventa;
DROP PROCEDURE IF EXISTS sp_anularventa;

DROP PROCEDURE IF EXISTS sp_insertarcategoriaproducto;
DROP PROCEDURE IF EXISTS sp_listarcategoriasproducto;
DROP PROCEDURE IF EXISTS sp_buscategoriaproducto;
DROP PROCEDURE IF EXISTS sp_actualizarcategoriaproducto;
DROP PROCEDURE IF EXISTS sp_eliminarcategoriaproducto;
DROP PROCEDURE IF EXISTS sp_cambiarestadocategoriaproducto;

DROP PROCEDURE IF EXISTS sp_insertarproducto;
DROP PROCEDURE IF EXISTS sp_listarproductos;
DROP PROCEDURE IF EXISTS sp_buscarproducto;
DROP PROCEDURE IF EXISTS sp_actualizarproducto;
DROP PROCEDURE IF EXISTS sp_eliminarproducto;
DROP PROCEDURE IF EXISTS sp_cambiarestadoproducto;

DROP PROCEDURE IF EXISTS sp_agregardetalleproducto;
DROP PROCEDURE IF EXISTS sp_actualizardetalleproducto;
DROP PROCEDURE IF EXISTS sp_eliminardetalleproducto;
DROP PROCEDURE IF EXISTS sp_listardetallesventa;

DROP PROCEDURE IF EXISTS sp_registrarmovimiento;
DROP PROCEDURE IF EXISTS sp_listarmovimientosinventario;
DROP PROCEDURE IF EXISTS sp_listarstockcritico;

DROP PROCEDURE IF EXISTS sp_verfactura;
DROP PROCEDURE IF EXISTS sp_obtenerdashboard;
DROP PROCEDURE IF EXISTS sp_reporte_ventas;
DROP PROCEDURE IF EXISTS sp_mostrartablas;


DROP PROCEDURE IF EXISTS sp_registrarmovimientoinventario;
DROP PROCEDURE IF EXISTS sp_listarmovimientosporproducto;
DROP PROCEDURE IF EXISTS sp_filtrarmovimientosinventario;
DROP PROCEDURE IF EXISTS sp_listarpeliculasactivas;
DROP PROCEDURE IF EXISTS sp_listarsalasactivas;
DROP PROCEDURE IF EXISTS sp_listarproductosactivos;
DROP PROCEDURE IF EXISTS sp_listarcartelera;
DROP PROCEDURE IF EXISTS sp_listarboletosventa;
DROP PROCEDURE IF EXISTS sp_buscarboleto;
DROP PROCEDURE IF EXISTS sp_quitarboleto;
DROP PROCEDURE IF EXISTS sp_agregarproductoventa;
DROP PROCEDURE IF EXISTS sp_listarproductosventa;
DROP PROCEDURE IF EXISTS sp_actualizarcantidadproducto;
DROP PROCEDURE IF EXISTS sp_quitarproductoventa;
DROP PROCEDURE IF EXISTS sp_eliminarventaabierta;
DROP PROCEDURE IF EXISTS sp_reporteventas_fechas;
DROP PROCEDURE IF EXISTS sp_reporteventasdia;
DROP PROCEDURE IF EXISTS sp_reporteventassemana;
DROP PROCEDURE IF EXISTS sp_reporteventasmes;
DROP PROCEDURE IF EXISTS sp_indicadoresadmin;


-- ============================================================
-- VISTAS DE APOYO PARA LOS DAO DE JAVA
-- ============================================================

CREATE OR REPLACE VIEW vw_lista_clientes AS
SELECT
    c.id_cliente,
    c.cui,
    c.nit,
    c.nombre_cliente,
    c.apellido_cliente,
    CONCAT(c.nombre_cliente, ' ', c.apellido_cliente) AS cliente,
    c.correo_electronico,
    c.telefono,
    c.estado,
    c.fecha_registro
FROM clientes c;

CREATE OR REPLACE VIEW vw_lista_generos AS
SELECT id_genero, nombre_genero, descripcion, estado
FROM generos;

CREATE OR REPLACE VIEW vw_lista_peliculas AS
SELECT
    p.id_pelicula,
    p.titulo,
    p.sinopsis,
    p.director,
    p.duracion_minutos,
    p.clasificacion,
    p.idioma,
    p.fecha_estreno,
    p.imagen,
    p.id_genero,
    g.nombre_genero,
    p.estado
FROM peliculas p
INNER JOIN generos g ON g.id_genero = p.id_genero;

CREATE OR REPLACE VIEW vw_lista_salas AS
SELECT
    s.id_sala,
    s.nombre_sala,
    s.formato,
    s.estado,
    (
        SELECT COUNT(*)
        FROM butacas b
        WHERE b.id_sala = s.id_sala
          AND b.estado = 1
    ) AS capacidad
FROM salas s;

CREATE OR REPLACE VIEW vw_lista_funciones AS
SELECT
    f.id_funcion,
    f.id_pelicula,
    p.titulo,
    p.id_genero,
    g.nombre_genero,
    p.duracion_minutos,
    p.clasificacion,
    p.idioma,
    f.id_sala,
    s.nombre_sala,
    s.formato,
    f.fecha_inicio,
    f.fecha_fin,
    f.precio_boleto,
    f.estado,
    (
        SELECT COUNT(*)
        FROM butacas bu
        WHERE bu.id_sala = f.id_sala
          AND bu.estado = 1
    ) AS capacidad,
    (
        SELECT COUNT(*)
        FROM boletos b
        WHERE b.id_funcion = f.id_funcion
          AND b.estado = 'reservado'
    ) AS boletos_reservados,
    (
        SELECT COUNT(*)
        FROM boletos b
        WHERE b.id_funcion = f.id_funcion
          AND b.estado = 'vendido'
    ) AS boletos_vendidos,
    (
        SELECT COUNT(*)
        FROM butacas bu
        WHERE bu.id_sala = f.id_sala
          AND bu.estado = 1
          AND NOT EXISTS (
              SELECT 1
              FROM boletos b
              WHERE b.id_funcion = f.id_funcion
                AND b.id_butaca = bu.id_butaca
                AND b.estado IN ('reservado','vendido')
          )
    ) AS butacas_disponibles
FROM funciones f
INNER JOIN peliculas p ON p.id_pelicula = f.id_pelicula
INNER JOIN generos g ON g.id_genero = p.id_genero
INNER JOIN salas s ON s.id_sala = f.id_sala;

CREATE OR REPLACE VIEW vw_cartelera AS
SELECT lf.*
FROM vw_lista_funciones lf
WHERE lf.estado = 'programada'
  AND lf.fecha_inicio > NOW()
  AND EXISTS (
      SELECT 1 FROM peliculas p
      WHERE p.id_pelicula = lf.id_pelicula AND p.estado = 1
  )
  AND EXISTS (
      SELECT 1 FROM salas s
      WHERE s.id_sala = lf.id_sala AND s.estado = 1
  );

CREATE OR REPLACE VIEW vw_lista_productos AS
SELECT
    p.id_producto,
    p.id_categoria_producto,
    c.nombre_categoria,
    p.nombre_producto,
    p.descripcion,
    p.precio,
    p.stock,
    p.stock_minimo,
    p.estado
FROM productos p
INNER JOIN categorias_producto c
    ON c.id_categoria_producto = p.id_categoria_producto;

CREATE OR REPLACE VIEW vw_stock_critico AS
SELECT *
FROM vw_lista_productos
WHERE estado = 1
  AND stock <= stock_minimo;

CREATE OR REPLACE VIEW vw_lista_boletos AS
SELECT
    b.id_boleto,
    b.id_venta,
    b.id_funcion,
    f.id_pelicula,
    p.titulo,
    b.id_sala,
    s.nombre_sala,
    b.id_butaca,
    bu.fila,
    bu.numero,
    CONCAT(bu.fila, bu.numero) AS butaca,
    f.fecha_inicio,
    f.fecha_fin,
    b.precio_unitario,
    b.estado
FROM boletos b
INNER JOIN funciones f ON f.id_funcion = b.id_funcion
INNER JOIN peliculas p ON p.id_pelicula = f.id_pelicula
INNER JOIN salas s ON s.id_sala = b.id_sala
INNER JOIN butacas bu ON bu.id_butaca = b.id_butaca;

CREATE OR REPLACE VIEW vw_lista_ventas AS
SELECT
    v.id_venta,
    v.fecha_venta,
    v.id_cliente,
    c.cui,
    c.nit,
    CONCAT(c.nombre_cliente, ' ', c.apellido_cliente) AS cliente,
    c.correo_electronico,
    v.id_usuario,
    CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS cajero,
    CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS taquillero,
    v.total,
    v.total AS total_venta,
    v.metodo_pago,
    v.estado,
    v.fecha_confirmacion,
    v.fecha_anulacion,
    v.motivo_anulacion,
    (
        SELECT COUNT(*)
        FROM boletos b
        WHERE b.id_venta = v.id_venta
          AND b.estado IN ('reservado','vendido')
    ) AS cantidad_boletos,
    (
        SELECT COALESCE(SUM(b.precio_unitario),0)
        FROM boletos b
        WHERE b.id_venta = v.id_venta
          AND b.estado IN ('reservado','vendido')
    ) AS total_boletos,
    (
        SELECT COALESCE(SUM(d.subtotal),0)
        FROM detalle_venta_productos d
        WHERE d.id_venta = v.id_venta
    ) AS total_productos
FROM ventas v
INNER JOIN clientes c ON c.id_cliente = v.id_cliente
INNER JOIN usuarios u ON u.id_usuario = v.id_usuario;

CREATE OR REPLACE VIEW vw_movimientos_inventario AS
SELECT
    m.id_movimiento,
    m.id_producto,
    p.nombre_producto,
    m.id_usuario,
    CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS usuario,
    m.id_venta,
    m.tipo_movimiento,
    m.cantidad,
    m.fecha_movimiento,
    m.observacion
FROM movimientos_inventario m
INNER JOIN productos p ON p.id_producto = m.id_producto
INNER JOIN usuarios u ON u.id_usuario = m.id_usuario;

CREATE OR REPLACE VIEW vw_factura_ventas AS
SELECT
    v.id_venta,
    v.fecha_venta,
    CONCAT(c.nombre_cliente, ' ', c.apellido_cliente) AS cliente,
    c.nit,
    c.cui,
    CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS cajero,
    v.total,
    v.metodo_pago,
    v.estado
FROM ventas v
INNER JOIN clientes c ON c.id_cliente = v.id_cliente
INNER JOIN usuarios u ON u.id_usuario = v.id_usuario;

CREATE OR REPLACE VIEW vw_reporte_ventas_diarias AS
SELECT
    DATE(fecha_venta) AS fecha,
    COUNT(*) AS cantidad_ventas,
    COALESCE(SUM(total),0) AS total_ventas
FROM ventas
WHERE estado = 'confirmada'
GROUP BY DATE(fecha_venta);


DELIMITER $$

-- ============================================================
-- USUARIOS Y LOGIN
-- ============================================================

CREATE PROCEDURE sp_buscarusuario_login(
    IN _username VARCHAR(50)
)
BEGIN
    SELECT
        u.id_usuario,
        u.nombre_usuario,
        u.apellido_usuario,
        u.username,
        u.correo_electronico,
        u.contrasena_hash,
        u.id_rol,
        r.nombre_rol,
        u.id_cliente,
        u.estado,
        u.fecha_registro
    FROM usuarios u
    INNER JOIN roles r
        ON r.id_rol = u.id_rol
    WHERE u.username = _username;
END $$

CREATE PROCEDURE sp_insertarusuario(
    IN _nombre_usuario VARCHAR(100),
    IN _apellido_usuario VARCHAR(100),
    IN _username VARCHAR(50),
    IN _correo_electronico VARCHAR(120),
    IN _id_rol INT,
    IN _id_cliente INT,
    IN _contrasena_hash VARCHAR(255)
)
BEGIN
    INSERT INTO usuarios (
        nombre_usuario,
        apellido_usuario,
        username,
        correo_electronico,
        id_rol,
        id_cliente,
        contrasena_hash,
        estado
    )
    VALUES (
        TRIM(_nombre_usuario),
        TRIM(_apellido_usuario),
        TRIM(_username),
        TRIM(_correo_electronico),
        _id_rol,
        _id_cliente,
        _contrasena_hash,
        1
    );

    SELECT LAST_INSERT_ID() AS id_usuario;
END $$

CREATE PROCEDURE sp_listarusuarios()
BEGIN
    SELECT
        u.id_usuario,
        u.nombre_usuario,
        u.apellido_usuario,
        u.username,
        u.correo_electronico,
        u.id_rol,
        r.nombre_rol,
        u.id_cliente,
        u.estado,
        u.fecha_registro
    FROM usuarios u
    INNER JOIN roles r
        ON r.id_rol = u.id_rol
    ORDER BY u.id_usuario;
END $$

CREATE PROCEDURE sp_buscarusuario(
    IN _id_usuario INT
)
BEGIN
    SELECT
        u.id_usuario,
        u.nombre_usuario,
        u.apellido_usuario,
        u.username,
        u.correo_electronico,
        u.id_rol,
        r.nombre_rol,
        u.id_cliente,
        u.estado,
        u.fecha_registro
    FROM usuarios u
    INNER JOIN roles r
        ON r.id_rol = u.id_rol
    WHERE u.id_usuario = _id_usuario;
END $$

CREATE PROCEDURE sp_actualizarusuario(
    IN _id_usuario INT,
    IN _nombre_usuario VARCHAR(100),
    IN _apellido_usuario VARCHAR(100),
    IN _username VARCHAR(50),
    IN _correo_electronico VARCHAR(120),
    IN _id_rol INT,
    IN _id_cliente INT
)
BEGIN
    UPDATE usuarios
    SET
        nombre_usuario = TRIM(_nombre_usuario),
        apellido_usuario = TRIM(_apellido_usuario),
        username = TRIM(_username),
        correo_electronico = TRIM(_correo_electronico),
        id_rol = _id_rol,
        id_cliente = _id_cliente
    WHERE id_usuario = _id_usuario;
END $$

CREATE PROCEDURE sp_eliminarusuario(
    IN _id_usuario INT
)
BEGIN
    DELETE FROM usuarios
    WHERE id_usuario = _id_usuario;
END $$

CREATE PROCEDURE sp_cambiarestadousuario(
    IN _id_usuario INT,
    IN _estado TINYINT
)
BEGIN
    UPDATE usuarios
    SET estado = _estado
    WHERE id_usuario = _id_usuario;
END $$

CREATE PROCEDURE sp_cambiarcontrasena(
    IN _id_usuario INT,
    IN _contrasena_hash VARCHAR(255)
)
BEGIN
    UPDATE usuarios
    SET contrasena_hash = _contrasena_hash
    WHERE id_usuario = _id_usuario;
END $$

CREATE PROCEDURE sp_listarroles()
BEGIN
    SELECT id_rol, nombre_rol, descripcion
    FROM roles
    ORDER BY id_rol;
END $$

-- ============================================================
-- CLIENTES
-- ============================================================

CREATE PROCEDURE sp_insertarcliente(
    IN _cui CHAR(13),
    IN _nombre_cliente VARCHAR(100),
    IN _apellido_cliente VARCHAR(100),
    IN _nit VARCHAR(20),
    IN _correo_electronico VARCHAR(120),
    IN _telefono VARCHAR(20)
)
BEGIN
    INSERT INTO clientes (
        cui, nombre_cliente, apellido_cliente, nit,
        correo_electronico, telefono, estado
    )
    VALUES (
        _cui,TRIM(_nombre_cliente), TRIM(_apellido_cliente),         COALESCE(NULLIF(TRIM(_nit), ''), 'CF'),
        TRIM(_correo_electronico), _telefono, 1
    );

    SELECT LAST_INSERT_ID() AS id_cliente;
END $$

CREATE PROCEDURE sp_listarclientes()
BEGIN
    SELECT
        id_cliente,
        cui,
        nit,
        nombre_cliente,
        apellido_cliente,
        correo_electronico,
        telefono,
        estado,
        fecha_registro
    FROM clientes
    ORDER BY id_cliente;
END $$

CREATE PROCEDURE sp_buscarcliente(
    IN _id_cliente INT
)
BEGIN
    SELECT
        id_cliente,
        cui,
        nit,
        nombre_cliente,
        apellido_cliente,
        correo_electronico,
        telefono,
        estado,
        fecha_registro
    FROM clientes
    WHERE id_cliente = _id_cliente;
END $$

CREATE PROCEDURE sp_actualizarcliente(
    IN _id_cliente INT,
    IN _cui CHAR(13),
    IN _nombre_cliente VARCHAR(100),
    IN _apellido_cliente VARCHAR(100),
    IN _nit VARCHAR(20),
    IN _correo_electronico VARCHAR(120),
    IN _telefono VARCHAR(20)
)
BEGIN
    UPDATE clientes
    SET
        cui = _cui,
        nombre_cliente = TRIM(_nombre_cliente),
        apellido_cliente = TRIM(_apellido_cliente),
        nit = COALESCE(NULLIF(TRIM(_nit), ''), 'CF'),
        correo_electronico = TRIM(_correo_electronico),
        telefono = _telefono
    WHERE id_cliente = _id_cliente;
END $$

CREATE PROCEDURE sp_eliminarcliente(
    IN _id_cliente INT
)
BEGIN
    DELETE FROM clientes
    WHERE id_cliente = _id_cliente;
END $$

CREATE PROCEDURE sp_cambiarestadocliente(
    IN _id_cliente INT,
    IN _estado TINYINT
)
BEGIN
    UPDATE clientes
    SET estado = _estado
    WHERE id_cliente = _id_cliente;
END $$

CREATE PROCEDURE sp_listarclientesactivos()
BEGIN
    SELECT
        id_cliente,
        cui,
        nit,
        nombre_cliente,
        apellido_cliente,
        correo_electronico,
        telefono
    FROM clientes
    WHERE estado = 1
    ORDER BY apellido_cliente, nombre_cliente;
END $$

-- ============================================================
-- GENEROS
-- ============================================================

CREATE PROCEDURE sp_insertargenero(
    IN _nombre_genero VARCHAR(100),
    IN _descripcion VARCHAR(200)
)
BEGIN
    INSERT INTO generos(nombre_genero, descripcion, estado)
    VALUES(TRIM(_nombre_genero), _descripcion, 1);

    SELECT LAST_INSERT_ID() AS id_genero;
END $$

CREATE PROCEDURE sp_listargeneros()
BEGIN
    SELECT id_genero, nombre_genero, descripcion, estado
    FROM generos
    ORDER BY id_genero;
END $$

CREATE PROCEDURE sp_buscargenero(
    IN _id_genero INT
)
BEGIN
    SELECT id_genero, nombre_genero, descripcion, estado
    FROM generos
    WHERE id_genero = _id_genero;
END $$

CREATE PROCEDURE sp_actualizargenero(
    IN _id_genero INT,
    IN _nombre_genero VARCHAR(100),
    IN _descripcion VARCHAR(200)
)
BEGIN
    UPDATE generos
    SET
        nombre_genero = TRIM(_nombre_genero),
        descripcion = _descripcion
    WHERE id_genero = _id_genero;
END $$

CREATE PROCEDURE sp_eliminargenero(
    IN _id_genero INT
)
BEGIN
    DELETE FROM generos
    WHERE id_genero = _id_genero;
END $$

CREATE PROCEDURE sp_cambiarestadogenero(
    IN _id_genero INT,
    IN _estado TINYINT
)
BEGIN
    UPDATE generos
    SET estado = _estado
    WHERE id_genero = _id_genero;
END $$

CREATE PROCEDURE sp_insertarpelicula(
    IN _titulo VARCHAR(150),
    IN _sinopsis TEXT,
    IN _director VARCHAR(150),
    IN _duracion_minutos INT,
    IN _clasificacion VARCHAR(50),
    IN _idioma VARCHAR(50),
    IN _fecha_estreno DATE,
    IN _imagen LONGBLOB,
    IN _id_genero INT
)
BEGIN
    INSERT INTO peliculas (
        titulo,
        sinopsis,
        director,
        duracion_minutos,
        clasificacion,
        idioma,
        fecha_estreno,
        imagen,
        id_genero,
        estado
    )
    VALUES (
        _titulo,
        _sinopsis,
        _director,
        _duracion_minutos,
        _clasificacion,
        _idioma,
        _fecha_estreno,
        _imagen,
        _id_genero,
        1
    );

    SELECT LAST_INSERT_ID() AS id_pelicula;
END $$

CREATE PROCEDURE sp_listarpeliculas()
BEGIN
    SELECT
        p.id_pelicula,
        p.titulo,
        p.sinopsis,
        p.director,
        p.duracion_minutos,
        p.clasificacion,
        p.idioma,
        p.fecha_estreno,
        p.imagen,
        p.id_genero,
        p.estado,
        g.nombre_genero
    FROM peliculas p
    INNER JOIN generos g
        ON g.id_genero = p.id_genero
    ORDER BY p.id_pelicula;
END $$

CREATE PROCEDURE sp_buscarpelicula(
    IN _id_pelicula INT
)
BEGIN
    SELECT
        p.id_pelicula,
        p.titulo,
        p.sinopsis,
        p.director,
        p.duracion_minutos,
        p.clasificacion,
        p.idioma,
        p.fecha_estreno,
        p.imagen,
        p.id_genero,
        p.estado,
        g.nombre_genero
    FROM peliculas p
    INNER JOIN generos g
        ON g.id_genero = p.id_genero
    WHERE p.id_pelicula = _id_pelicula;
END $$

CREATE PROCEDURE sp_actualizarpelicula(
    IN _id_pelicula INT,
    IN _titulo VARCHAR(150),
    IN _sinopsis TEXT,
    IN _director VARCHAR(150),
    IN _duracion_minutos INT,
    IN _clasificacion VARCHAR(50),
    IN _idioma VARCHAR(50),
    IN _fecha_estreno DATE,
    IN _imagen LONGBLOB,
    IN _id_genero INT
)
BEGIN
    UPDATE peliculas
    SET
        titulo = _titulo,
        sinopsis = _sinopsis,
        director = _director,
        duracion_minutos = _duracion_minutos,
        clasificacion = _clasificacion,
        idioma = _idioma,
        fecha_estreno = _fecha_estreno,
        imagen = _imagen,
        id_genero = _id_genero
    WHERE id_pelicula = _id_pelicula;
END $$

CREATE PROCEDURE sp_eliminarpelicula(
    IN _id_pelicula INT
)
BEGIN
    DELETE FROM peliculas
    WHERE id_pelicula = _id_pelicula;
END $$

CREATE PROCEDURE sp_cambiarestadopelicula(
    IN _id_pelicula INT,
    IN _estado TINYINT
)
BEGIN
    IF _estado NOT IN (0,1) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El estado debe ser 0 o 1.';
    END IF;

    IF _estado = 0 AND EXISTS (
        SELECT 1
        FROM funciones
        WHERE id_pelicula = _id_pelicula
          AND estado = 'programada'
          AND fecha_fin > NOW()
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'No se puede desactivar la pelicula porque tiene funciones programadas.';
    END IF;

    UPDATE peliculas
    SET estado = _estado
    WHERE id_pelicula = _id_pelicula;
END $$

-- ============================================================
-- SALAS
-- ============================================================

CREATE PROCEDURE sp_insertarsala(
    IN _nombre_sala VARCHAR(100),
    IN _formato VARCHAR(20)
)
BEGIN
    INSERT INTO salas(nombre_sala, formato, estado)
    VALUES(TRIM(_nombre_sala), _formato, 1);

    SELECT LAST_INSERT_ID() AS id_sala;
END $$

CREATE PROCEDURE sp_listarsalas()
BEGIN
    SELECT id_sala, nombre_sala, formato, estado
    FROM salas
    ORDER BY id_sala;
END $$

CREATE PROCEDURE sp_buscarsala(
    IN _id_sala INT
)
BEGIN
    SELECT id_sala, nombre_sala, formato, estado
    FROM salas
    WHERE id_sala = _id_sala;
END $$

CREATE PROCEDURE sp_actualizarsala(
    IN _id_sala INT,
    IN _nombre_sala VARCHAR(100),
    IN _formato VARCHAR(20)
)
BEGIN
    UPDATE salas
    SET nombre_sala = TRIM(_nombre_sala),
        formato = _formato
    WHERE id_sala = _id_sala;
END $$

CREATE PROCEDURE sp_eliminarsala(
    IN _id_sala INT
)
BEGIN
    DELETE FROM salas
    WHERE id_sala = _id_sala;
END $$

CREATE PROCEDURE sp_cambiarestadosala(
    IN _id_sala INT,
    IN _estado TINYINT
)
BEGIN
    IF _estado NOT IN (0,1) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El estado debe ser 0 o 1.';
    END IF;

    IF _estado = 0 AND EXISTS (
        SELECT 1
        FROM funciones
        WHERE id_sala = _id_sala
          AND estado = 'programada'
          AND fecha_fin > NOW()
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No se puede desactivar la sala porque tiene funciones programadas.';
    END IF;

    UPDATE salas
    SET estado = _estado
    WHERE id_sala = _id_sala;
END $$

-- ============================================================
-- BUTACAS
-- ============================================================

CREATE PROCEDURE sp_insertarbutaca(
    IN _id_sala INT,
    IN _fila VARCHAR(3),
    IN _numero INT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM salas
        WHERE id_sala = _id_sala AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La sala no existe o esta inactiva.';
    END IF;

    INSERT INTO butacas(
        id_sala,
        fila,
        numero,
        estado
    )
    VALUES(
        _id_sala,
        UPPER(TRIM(_fila)),
        _numero,
        1
    );

    SELECT LAST_INSERT_ID() AS id_butaca;
END $$

CREATE PROCEDURE sp_listarbutacas()
BEGIN
    SELECT
        b.id_butaca,
        b.id_sala,
        s.nombre_sala,
        b.fila,
        b.numero,
        b.estado
    FROM butacas b
    INNER JOIN salas s
        ON s.id_sala = b.id_sala
    ORDER BY b.id_sala, b.fila, b.numero;
END $$

CREATE PROCEDURE sp_buscarbutaca(
    IN _id_butaca INT
)
BEGIN
    SELECT
        b.id_butaca,
        b.id_sala,
        s.nombre_sala,
        b.fila,
        b.numero,
        b.estado
    FROM butacas b
    INNER JOIN salas s
        ON s.id_sala = b.id_sala
    WHERE b.id_butaca = _id_butaca;
END $$

CREATE PROCEDURE sp_actualizarbutaca(
    IN _id_butaca INT,
    IN _id_sala INT,
    IN _fila VARCHAR(3),
    IN _numero INT
)
BEGIN
    DECLARE _existe INT DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT id_butaca
    INTO _existe
    FROM butacas
    WHERE id_butaca = _id_butaca
    FOR UPDATE;

    IF _existe IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La butaca no existe.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM salas
        WHERE id_sala = _id_sala AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La sala no existe o esta inactiva.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM boletos
        WHERE id_butaca = _id_butaca
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Una butaca con historial no puede cambiar de ubicacion.';
    END IF;

    UPDATE butacas
    SET
        id_sala = _id_sala,
        fila = UPPER(TRIM(_fila)),
        numero = _numero
    WHERE id_butaca = _id_butaca;

    COMMIT;
END $$

CREATE PROCEDURE sp_eliminarbutaca(
    IN _id_butaca INT
)
BEGIN
    DELETE FROM butacas
    WHERE id_butaca = _id_butaca;
END $$

CREATE PROCEDURE sp_cambiarestadobutaca(
    IN _id_butaca INT,
    IN _estado TINYINT
)
BEGIN
    IF _estado NOT IN (0,1) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El estado debe ser 0 o 1.';
    END IF;

    IF _estado = 0 AND EXISTS (
        SELECT 1
        FROM boletos b
        INNER JOIN funciones f ON f.id_funcion = b.id_funcion
        WHERE b.id_butaca = _id_butaca
          AND b.estado IN ('reservado','vendido')
          AND f.estado = 'programada'
          AND f.fecha_fin > NOW()
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La butaca tiene boletos activos para una funcion pendiente.';
    END IF;

    UPDATE butacas
    SET estado = _estado
    WHERE id_butaca = _id_butaca;
END $$

-- ============================================================
-- FUNCIONES
-- ============================================================

CREATE PROCEDURE sp_insertarfuncion(
    IN _id_pelicula INT,
    IN _id_sala INT,
    IN _fecha_inicio DATETIME,
    IN _precio_boleto DECIMAL(10,2)
)
BEGIN
    DECLARE _duracion INT DEFAULT NULL;
    DECLARE _fecha_fin DATETIME;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF _fecha_inicio IS NULL OR _fecha_inicio <= NOW() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La funcion debe programarse para una fecha futura.';
    END IF;

    IF _precio_boleto IS NULL OR _precio_boleto <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El precio del boleto debe ser mayor que cero.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM peliculas
        WHERE id_pelicula = _id_pelicula
          AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Las peliculas inactivas no pueden programarse.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM salas
        WHERE id_sala = _id_sala
          AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Las salas inactivas no pueden programarse.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM butacas
        WHERE id_sala = _id_sala
          AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La sala debe tener butacas activas.';
    END IF;

    SELECT duracion_minutos
    INTO _duracion
    FROM peliculas
    WHERE id_pelicula = _id_pelicula;

    SET _fecha_fin = DATE_ADD(
        _fecha_inicio,
        INTERVAL (_duracion + 20) MINUTE
    );

    IF _fecha_fin IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La fecha final queda fuera del rango permitido.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM funciones
        WHERE id_sala = _id_sala
          AND estado <> 'cancelada'
          AND _fecha_inicio < fecha_fin
          AND _fecha_fin > fecha_inicio
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El horario se cruza con otra funcion de la sala.';
    END IF;

    INSERT INTO funciones(
        id_pelicula,
        id_sala,
        fecha_inicio,
        fecha_fin,
        precio_boleto,
        estado
    )
    VALUES(
        _id_pelicula,
        _id_sala,
        _fecha_inicio,
        _fecha_fin,
        _precio_boleto,
        'programada'
    );

    SET @cine_id_funcion_nueva = LAST_INSERT_ID();

    COMMIT;

    SELECT @cine_id_funcion_nueva AS id_funcion;
END $$

CREATE PROCEDURE sp_listarfunciones()
BEGIN
    SELECT
        f.id_funcion,
        f.id_pelicula,
        p.titulo AS pelicula,
        f.id_sala,
        s.nombre_sala AS sala,
        f.fecha_inicio,
        f.fecha_fin,
        f.precio_boleto,
        f.estado
    FROM funciones f
    INNER JOIN peliculas p
        ON p.id_pelicula = f.id_pelicula
    INNER JOIN salas s
        ON s.id_sala = f.id_sala
    ORDER BY f.fecha_inicio, f.id_sala;
END $$

CREATE PROCEDURE sp_buscarfuncion(
    IN _id_funcion INT
)
BEGIN
    SELECT
        f.id_funcion,
        f.id_pelicula,
        p.titulo AS pelicula,
        f.id_sala,
        s.nombre_sala AS sala,
        f.fecha_inicio,
        f.fecha_fin,
        f.precio_boleto,
        f.estado
    FROM funciones f
    INNER JOIN peliculas p
        ON p.id_pelicula = f.id_pelicula
    INNER JOIN salas s
        ON s.id_sala = f.id_sala
    WHERE f.id_funcion = _id_funcion;
END $$

CREATE PROCEDURE sp_actualizarfuncion(
    IN _id_funcion INT,
    IN _id_pelicula INT,
    IN _id_sala INT,
    IN _fecha_inicio DATETIME,
    IN _precio_boleto DECIMAL(10,2)
)
BEGIN
    DECLARE _duracion INT DEFAULT NULL;
    DECLARE _fecha_fin DATETIME;
    DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE _inicio_anterior DATETIME DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF _fecha_inicio IS NULL OR _fecha_inicio <= NOW() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La funcion debe programarse para una fecha futura.';
    END IF;

    IF _precio_boleto IS NULL OR _precio_boleto <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El precio del boleto debe ser mayor que cero.';
    END IF;

    SELECT estado, fecha_inicio
    INTO _estado, _inicio_anterior
    FROM funciones
    WHERE id_funcion = _id_funcion
    FOR UPDATE;

    IF _estado IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La funcion no existe.';
    END IF;

    IF _estado <> 'programada' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Solo se puede editar una funcion programada.';
    END IF;

    IF _inicio_anterior <= NOW() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No se puede editar una funcion que ya comenzo.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM peliculas
        WHERE id_pelicula = _id_pelicula
          AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Las peliculas inactivas no pueden programarse.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM salas
        WHERE id_sala = _id_sala
          AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Las salas inactivas no pueden programarse.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM butacas
        WHERE id_sala = _id_sala
          AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La sala debe tener butacas activas.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM boletos
        WHERE id_funcion = _id_funcion
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La funcion tiene historial de boletos; crear otra funcion.';
    END IF;

    SELECT duracion_minutos
    INTO _duracion
    FROM peliculas
    WHERE id_pelicula = _id_pelicula;

    SET _fecha_fin = DATE_ADD(
        _fecha_inicio,
        INTERVAL (_duracion + 20) MINUTE
    );

    IF _fecha_fin IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La fecha final queda fuera del rango permitido.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM funciones
        WHERE id_sala = _id_sala
          AND id_funcion <> _id_funcion
          AND estado <> 'cancelada'
          AND _fecha_inicio < fecha_fin
          AND _fecha_fin > fecha_inicio
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El horario se cruza con otra funcion.';
    END IF;

    UPDATE funciones
    SET
        id_pelicula = _id_pelicula,
        id_sala = _id_sala,
        fecha_inicio = _fecha_inicio,
        fecha_fin = _fecha_fin,
        precio_boleto = _precio_boleto
    WHERE id_funcion = _id_funcion;

    COMMIT;
END $$

CREATE PROCEDURE sp_eliminarfuncion(
    IN _id_funcion INT
)
BEGIN
    DELETE FROM funciones
    WHERE id_funcion = _id_funcion;
END $$

CREATE PROCEDURE sp_cancelarfuncion(
    IN _id_funcion INT
)
BEGIN
    DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT estado
    INTO _estado
    FROM funciones
    WHERE id_funcion = _id_funcion
    FOR UPDATE;

    IF _estado IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La funcion no existe.';
    END IF;

    IF _estado <> 'programada' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Solo se puede cancelar una funcion programada.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM boletos
        WHERE id_funcion = _id_funcion
          AND estado IN ('reservado','vendido')
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Anular primero las ventas que tengan boletos de esta funcion.';
    END IF;

    UPDATE funciones
    SET estado = 'cancelada'
    WHERE id_funcion = _id_funcion;

    COMMIT;
END $$

CREATE PROCEDURE sp_finalizarfuncion(
    IN _id_funcion INT
)
BEGIN
    DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE _fecha_fin DATETIME DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT estado, fecha_fin
    INTO _estado, _fecha_fin
    FROM funciones
    WHERE id_funcion = _id_funcion
    FOR UPDATE;

    IF _estado IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La funcion no existe.';
    END IF;

    IF _estado = 'finalizada' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La funcion ya termino y no puede finalizarse nuevamente.';
    END IF;

    IF _estado <> 'programada' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Solo se puede finalizar una funcion programada.';
    END IF;

    IF _fecha_fin > NOW() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El horario de la funcion aun no finaliza.';
    END IF;

    UPDATE funciones
    SET estado = 'finalizada'
    WHERE id_funcion = _id_funcion;

    COMMIT;
END $$

CREATE PROCEDURE sp_listarbutacasfuncion(
    IN _id_funcion INT
)
BEGIN
    SELECT
        b.id_butaca,
        b.id_sala,
        b.fila,
        b.numero,
        b.estado,
        CASE
            WHEN b.estado = 0 THEN 'INACTIVA'
            WHEN EXISTS (
                SELECT 1
                FROM boletos bo
                WHERE bo.id_funcion = _id_funcion
                  AND bo.id_butaca = b.id_butaca
                  AND bo.estado IN ('reservado','vendido')
            ) THEN 'OCUPADA'
            ELSE 'DISPONIBLE'
        END AS disponibilidad
    FROM funciones f
    INNER JOIN butacas b
        ON b.id_sala = f.id_sala
    WHERE f.id_funcion = _id_funcion
    ORDER BY b.fila, b.numero;
END $$

CREATE PROCEDURE sp_consultarbutacasdisponibles(
    IN _id_funcion INT
)
BEGIN
    SELECT
        b.id_butaca,
        b.fila,
        b.numero,
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM boletos bo
                WHERE bo.id_funcion = _id_funcion
                  AND bo.id_butaca = b.id_butaca
                  AND bo.estado IN ('reservado','vendido')
            ) THEN 'OCUPADA'
            ELSE 'DISPONIBLE'
        END AS estado_disponibilidad
    FROM funciones f
    INNER JOIN butacas b
        ON b.id_sala = f.id_sala
    WHERE f.id_funcion = _id_funcion
      AND b.estado = 1
    ORDER BY b.fila, b.numero;
END $$

-- ============================================================
-- VENTAS Y BOLETOS
-- ============================================================

CREATE PROCEDURE sp_abrirventa(
    IN _id_cliente INT,
    IN _id_usuario INT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM clientes
        WHERE id_cliente = _id_cliente AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Debe seleccionar un cliente activo.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM usuarios u
        INNER JOIN roles r ON r.id_rol = u.id_rol
        WHERE u.id_usuario = _id_usuario
          AND u.estado = 1
          AND r.nombre_rol IN ('admin','taquillero')
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La venta requiere un taquillero o administrador activo.';
    END IF;

    INSERT INTO ventas(
        id_cliente,
        id_usuario,
        total,
        metodo_pago,
        estado
    )
    VALUES(
        _id_cliente,
        _id_usuario,
        0.00,
        'EFECTIVO',
        'abierta'
    );

    SELECT LAST_INSERT_ID() AS id_venta;
END $$

CREATE PROCEDURE sp_listarventas()
BEGIN
    SELECT *
    FROM vw_lista_ventas
    ORDER BY fecha_venta DESC, id_venta DESC;
END $$

CREATE PROCEDURE sp_buscarventa(
    IN _id_venta INT
)
BEGIN
    SELECT *
    FROM vw_lista_ventas
    WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_actualizarclienteventa(
    IN _id_venta INT,
    IN _id_cliente INT
)
BEGIN
    DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT estado
    INTO _estado
    FROM ventas
    WHERE id_venta = _id_venta
    FOR UPDATE;

    IF _estado IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;

    IF _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM clientes
        WHERE id_cliente = _id_cliente AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Seleccionar un cliente activo.';
    END IF;

    UPDATE ventas
    SET id_cliente = _id_cliente
    WHERE id_venta = _id_venta;

    COMMIT;
END $$

CREATE PROCEDURE sp_agregarboleto(
    IN _id_venta INT,
    IN _id_funcion INT,
    IN _id_butaca INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE _sala INT DEFAULT NULL;
    DECLARE _precio DECIMAL(10,2) DEFAULT NULL;
    DECLARE _inicio DATETIME DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT estado
    INTO _estado_venta
    FROM ventas
    WHERE id_venta = _id_venta
    FOR UPDATE;

    IF _estado_venta IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;

    IF _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;

    SELECT f.id_sala, f.precio_boleto, f.fecha_inicio
    INTO _sala, _precio, _inicio
    FROM funciones f
    INNER JOIN peliculas p ON p.id_pelicula = f.id_pelicula
    INNER JOIN salas s ON s.id_sala = f.id_sala
    WHERE f.id_funcion = _id_funcion
      AND f.estado = 'programada'
      AND f.fecha_inicio > NOW()
      AND p.estado = 1
      AND s.estado = 1
    FOR UPDATE;

    IF _sala IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La funcion no esta disponible.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM butacas
        WHERE id_butaca = _id_butaca
          AND id_sala = _sala
          AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La butaca no pertenece a la sala o esta inactiva.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM boletos
        WHERE id_funcion = _id_funcion
          AND id_butaca = _id_butaca
          AND estado IN ('reservado','vendido')
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La butaca ya esta ocupada.';
    END IF;

    INSERT INTO boletos(
        id_venta,
        id_funcion,
        id_butaca,
        id_sala,
        precio_unitario,
        estado
    )
    VALUES(
        _id_venta,
        _id_funcion,
        _id_butaca,
        _sala,
        _precio,
        'reservado'
    );

    UPDATE ventas
    SET total =
        COALESCE((
            SELECT SUM(precio_unitario)
            FROM boletos
            WHERE id_venta = _id_venta
              AND estado IN ('reservado','vendido')
        ),0)
        +
        COALESCE((
            SELECT SUM(subtotal)
            FROM detalle_venta_productos
            WHERE id_venta = _id_venta
        ),0)
    WHERE id_venta = _id_venta;

    COMMIT;

    SELECT LAST_INSERT_ID() AS id_boleto;
END $$

CREATE PROCEDURE sp_eliminarboleto(
    IN _id_boleto INT
)
BEGIN
    DECLARE _id_venta INT;

    SELECT id_venta
    INTO _id_venta
    FROM boletos
    WHERE id_boleto = _id_boleto;

    DELETE FROM boletos
    WHERE id_boleto = _id_boleto
      AND estado = 'reservado';

    UPDATE ventas
    SET total = (
        SELECT COALESCE(SUM(precio_unitario),0)
        FROM boletos
        WHERE id_venta = _id_venta
          AND estado IN ('reservado','vendido')
    )
    WHERE id_venta = _id_venta
      AND estado = 'abierta';
END $$

CREATE PROCEDURE sp_confirmarventa(
    IN _id_venta INT,
    IN _metodo_pago VARCHAR(30)
)
BEGIN
    DECLARE _id_usuario INT DEFAULT NULL;
    DECLARE _id_cliente INT DEFAULT NULL;
    DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE _boletos INT DEFAULT 0;
    DECLARE _detalles INT DEFAULT 0;
    DECLARE _actualizados INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT id_usuario, id_cliente, estado
    INTO _id_usuario, _id_cliente, _estado
    FROM ventas
    WHERE id_venta = _id_venta
    FOR UPDATE;

    IF _estado IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;

    IF _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La venta no esta abierta.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM clientes
        WHERE id_cliente = _id_cliente
          AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La venta requiere un cliente activo.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM usuarios u
        INNER JOIN roles r ON r.id_rol = u.id_rol
        WHERE u.id_usuario = _id_usuario
          AND u.estado = 1
          AND r.nombre_rol IN ('admin','taquillero')
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El vendedor no esta activo o no tiene permiso.';
    END IF;

    SELECT COUNT(*) INTO _boletos
    FROM boletos
    WHERE id_venta = _id_venta
      AND estado = 'reservado';

    SELECT COUNT(*) INTO _detalles
    FROM detalle_venta_productos
    WHERE id_venta = _id_venta;

    IF _boletos = 0 AND _detalles = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No se puede confirmar un carrito vacio.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM boletos b
        INNER JOIN funciones f ON f.id_funcion = b.id_funcion
        INNER JOIN peliculas p ON p.id_pelicula = f.id_pelicula
        INNER JOIN salas s ON s.id_sala = f.id_sala
        INNER JOIN butacas bu ON bu.id_butaca = b.id_butaca
        WHERE b.id_venta = _id_venta
          AND (
              b.estado <> 'reservado'
              OR (
              f.estado <> 'programada'
              OR f.fecha_inicio <= NOW()
              OR p.estado <> 1
              OR s.estado <> 1
              OR bu.estado <> 1
          )
        )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Hay un boleto que ya no esta disponible.';
    END IF;

    UPDATE productos p
    INNER JOIN (
        SELECT id_producto, SUM(cantidad) AS cantidad_total
        FROM detalle_venta_productos
        WHERE id_venta = _id_venta
        GROUP BY id_producto
    ) d ON d.id_producto = p.id_producto
    SET p.stock = p.stock - d.cantidad_total
    WHERE p.estado = 1
      AND p.stock >= d.cantidad_total;

    SET _actualizados = ROW_COUNT();

    IF _actualizados <> _detalles THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No hay suficiente stock o existe un producto inactivo.';
    END IF;
    INSERT INTO movimientos_inventario(
        id_producto,
        id_usuario,
        id_venta,
        tipo_movimiento,
        cantidad,
        observacion
    )
    SELECT
        d.id_producto,
        _id_usuario,
        _id_venta,
        'SALIDA',
        d.cantidad,
        CONCAT('Venta #', _id_venta)
    FROM detalle_venta_productos d
    WHERE d.id_venta = _id_venta;

    UPDATE boletos
    SET estado = 'vendido'
    WHERE id_venta = _id_venta
      AND estado = 'reservado';

    UPDATE ventas
    SET
        estado = 'confirmada',
        metodo_pago = COALESCE(NULLIF(TRIM(_metodo_pago), ''), 'EFECTIVO'),
        fecha_confirmacion = NOW(),
        total =
            COALESCE((
                SELECT SUM(precio_unitario)
                FROM boletos
                WHERE id_venta = _id_venta
                  AND estado = 'vendido'
            ),0)
            +
            COALESCE((
                SELECT SUM(subtotal)
                FROM detalle_venta_productos
                WHERE id_venta = _id_venta
            ),0)
    WHERE id_venta = _id_venta;

    COMMIT;

    SELECT *
    FROM vw_lista_ventas
    WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_anularventa(
    IN _id_venta INT,
    IN _motivo VARCHAR(200)
)
BEGIN
    DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE _id_usuario INT DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT estado, id_usuario
    INTO _estado, _id_usuario
    FROM ventas
    WHERE id_venta = _id_venta
    FOR UPDATE;

    IF _estado IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;

    IF _estado = 'anulada' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La venta ya esta anulada.';
    END IF;

    IF _motivo IS NULL OR CHAR_LENGTH(TRIM(_motivo)) = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Indicar el motivo de la anulacion.';
    END IF;

    IF _estado = 'confirmada' THEN
        IF NOT EXISTS (
            SELECT 1
            FROM usuarios u
            INNER JOIN roles r ON r.id_rol = u.id_rol
            WHERE u.id_usuario = _id_usuario
              AND u.estado = 1
              AND r.nombre_rol IN ('admin','taquillero')
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La anulacion requiere un usuario autorizado.';
        END IF;

        UPDATE productos p
        INNER JOIN (
            SELECT id_producto, SUM(cantidad) AS cantidad_total
            FROM detalle_venta_productos
            WHERE id_venta = _id_venta
            GROUP BY id_producto
        ) d ON d.id_producto = p.id_producto
        SET p.stock = p.stock + d.cantidad_total;

        INSERT INTO movimientos_inventario(
            id_producto,
            id_usuario,
            id_venta,
            tipo_movimiento,
            cantidad,
            observacion
        )
        SELECT
            d.id_producto,
            _id_usuario,
            _id_venta,
            'ENTRADA',
            d.cantidad,
            CONCAT('Devolucion de venta #', _id_venta)
        FROM detalle_venta_productos d
        WHERE d.id_venta = _id_venta;
    END IF;

    UPDATE boletos
    SET estado = 'anulado'
    WHERE id_venta = _id_venta
      AND estado IN ('reservado','vendido');

    UPDATE ventas
    SET
        estado = 'anulada',
        fecha_anulacion = NOW(),
        motivo_anulacion = TRIM(_motivo)
    WHERE id_venta = _id_venta;

    COMMIT;
END $$

-- ============================================================
-- CATEGORIAS DE PRODUCTO
-- ============================================================

CREATE PROCEDURE sp_insertarcategoriaproducto(
    IN _nombre_categoria VARCHAR(100),
    IN _descripcion VARCHAR(255)
)
BEGIN
    INSERT INTO categorias_producto(
        nombre_categoria, descripcion, estado
    )
    VALUES(
        TRIM(_nombre_categoria), _descripcion, 1
    );

    SELECT LAST_INSERT_ID() AS id_categoria_producto;
END $$

CREATE PROCEDURE sp_listarcategoriasproducto()
BEGIN
    SELECT
        id_categoria_producto,
        nombre_categoria,
        descripcion,
        estado
    FROM categorias_producto
    ORDER BY id_categoria_producto;
END $$

CREATE PROCEDURE sp_buscategoriaproducto(
    IN _id_categoria_producto INT
)
BEGIN
    SELECT
        id_categoria_producto,
        nombre_categoria,
        descripcion,
        estado
    FROM categorias_producto
    WHERE id_categoria_producto = _id_categoria_producto;
END $$

CREATE PROCEDURE sp_actualizarcategoriaproducto(
    IN _id_categoria_producto INT,
    IN _nombre_categoria VARCHAR(100),
    IN _descripcion VARCHAR(255)
)
BEGIN
    UPDATE categorias_producto
    SET
        nombre_categoria = TRIM(_nombre_categoria),
        descripcion = _descripcion
    WHERE id_categoria_producto = _id_categoria_producto;
END $$

CREATE PROCEDURE sp_eliminarcategoriaproducto(
    IN _id_categoria_producto INT
)
BEGIN
    DELETE FROM categorias_producto
    WHERE id_categoria_producto = _id_categoria_producto;
END $$

CREATE PROCEDURE sp_cambiarestadocategoriaproducto(
    IN _id_categoria_producto INT,
    IN _estado TINYINT
)
BEGIN
    UPDATE categorias_producto
    SET estado = _estado
    WHERE id_categoria_producto = _id_categoria_producto;
END $$

-- ============================================================
-- PRODUCTOS
-- ============================================================

CREATE PROCEDURE sp_insertarproducto(
    IN _id_categoria_producto INT,
    IN _nombre_producto VARCHAR(100),
    IN _descripcion VARCHAR(255),
    IN _precio DECIMAL(10,2),
    IN _stock INT,
    IN _stock_minimo INT
)
BEGIN
    INSERT INTO productos(
        id_categoria_producto,
        nombre_producto,
        descripcion,
        precio,
        stock,
        stock_minimo,
        estado
    )
    VALUES(
        _id_categoria_producto,
        TRIM(_nombre_producto),
        _descripcion,
        _precio,
        _stock,
        _stock_minimo,
        1
    );

    SELECT LAST_INSERT_ID() AS id_producto;
END $$

CREATE PROCEDURE sp_listarproductos()
BEGIN
    SELECT
        p.id_producto,
        p.id_categoria_producto,
        c.nombre_categoria,
        p.nombre_producto,
        p.descripcion,
        p.precio,
        p.stock,
        p.stock_minimo,
        p.estado
    FROM productos p
    INNER JOIN categorias_producto c
        ON c.id_categoria_producto = p.id_categoria_producto
    ORDER BY p.id_producto;
END $$

CREATE PROCEDURE sp_buscarproducto(
    IN _id_producto INT
)
BEGIN
    SELECT
        p.id_producto,
        p.id_categoria_producto,
        c.nombre_categoria,
        p.nombre_producto,
        p.descripcion,
        p.precio,
        p.stock,
        p.stock_minimo,
        p.estado
    FROM productos p
    INNER JOIN categorias_producto c
        ON c.id_categoria_producto = p.id_categoria_producto
    WHERE p.id_producto = _id_producto;
END $$

CREATE PROCEDURE sp_actualizarproducto(
    IN _id_producto INT,
    IN _id_categoria_producto INT,
    IN _nombre_producto VARCHAR(100),
    IN _descripcion VARCHAR(255),
    IN _precio DECIMAL(10,2),
    IN _stock_minimo INT
)
BEGIN
    UPDATE productos
    SET
        id_categoria_producto = _id_categoria_producto,
        nombre_producto = TRIM(_nombre_producto),
        descripcion = _descripcion,
        precio = _precio,
        stock_minimo = _stock_minimo
    WHERE id_producto = _id_producto;
END $$

CREATE PROCEDURE sp_eliminarproducto(
    IN _id_producto INT
)
BEGIN
    DELETE FROM productos
    WHERE id_producto = _id_producto;
END $$

CREATE PROCEDURE sp_cambiarestadoproducto(
    IN _id_producto INT,
    IN _estado TINYINT
)
BEGIN
    UPDATE productos
    SET estado = _estado
    WHERE id_producto = _id_producto;
END $$

-- ============================================================
-- DETALLE DE PRODUCTOS
-- ============================================================

CREATE PROCEDURE sp_agregardetalleproducto(
    IN _id_venta INT,
    IN _id_producto INT,
    IN _cantidad INT
)
BEGIN
    DECLARE _precio DECIMAL(10,2);
    DECLARE _stock INT;
    DECLARE _cantidad_actual INT DEFAULT 0;
    DECLARE _cantidad_nueva INT;

    IF _cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La cantidad debe ser mayor que cero.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM ventas
        WHERE id_venta = _id_venta
          AND estado = 'abierta'
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La venta no esta abierta.';
    END IF;

    SELECT precio, stock
    INTO _precio, _stock
    FROM productos
    WHERE id_producto = _id_producto
      AND estado = 1;

    IF _precio IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El producto no existe o esta inactivo.';
    END IF;

    SELECT COALESCE(SUM(cantidad),0)
    INTO _cantidad_actual
    FROM detalle_venta_productos
    WHERE id_venta = _id_venta
      AND id_producto = _id_producto;

    SET _cantidad_nueva = _cantidad_actual + _cantidad;

    IF _stock < _cantidad_nueva THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Stock insuficiente.';
    END IF;

    IF _cantidad_actual > 0 THEN
        UPDATE detalle_venta_productos
        SET
            cantidad = _cantidad_nueva,
            subtotal = _cantidad_nueva * precio_unitario
        WHERE id_venta = _id_venta
          AND id_producto = _id_producto;
    ELSE
        INSERT INTO detalle_venta_productos(
            id_venta,
            id_producto,
            cantidad,
            precio_unitario,
            subtotal
        )
        VALUES(
            _id_venta,
            _id_producto,
            _cantidad,
            _precio,
            _cantidad * _precio
        );
    END IF;

    UPDATE ventas
    SET total =
        COALESCE((
            SELECT SUM(precio_unitario)
            FROM boletos
            WHERE id_venta = _id_venta
              AND estado IN ('reservado','vendido')
        ),0)
        +
        COALESCE((
            SELECT SUM(subtotal)
            FROM detalle_venta_productos
            WHERE id_venta = _id_venta
        ),0)
    WHERE id_venta = _id_venta;

    SELECT id_venta, total
    FROM ventas
    WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_actualizardetalleproducto(
    IN _id_detalle_producto INT,
    IN _cantidad INT
)
BEGIN
    DECLARE _id_venta INT DEFAULT NULL;
    DECLARE _id_producto INT DEFAULT NULL;
    DECLARE _precio DECIMAL(10,2) DEFAULT NULL;
    DECLARE _stock INT DEFAULT NULL;
    DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT d.id_venta, d.id_producto, d.precio_unitario
    INTO _id_venta, _id_producto, _precio
    FROM detalle_venta_productos d
    WHERE d.id_detalle_producto = _id_detalle_producto
    FOR UPDATE;

    IF _id_venta IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El detalle de venta no existe.';
    END IF;

    SELECT estado
    INTO _estado
    FROM ventas
    WHERE id_venta = _id_venta
    FOR UPDATE;

    IF _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;

    IF _cantidad IS NULL OR _cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La cantidad debe ser mayor que cero.';
    END IF;

    SELECT stock
    INTO _stock
    FROM productos
    WHERE id_producto = _id_producto
      AND estado = 1
    FOR UPDATE;

    IF _stock IS NULL OR _stock < _cantidad THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Stock insuficiente.';
    END IF;

    UPDATE detalle_venta_productos
    SET
        cantidad = _cantidad,
        subtotal = _cantidad * _precio
    WHERE id_detalle_producto = _id_detalle_producto;

    UPDATE ventas
    SET total =
        COALESCE((
            SELECT SUM(precio_unitario)
            FROM boletos
            WHERE id_venta = _id_venta
              AND estado IN ('reservado','vendido')
        ),0)
        +
        COALESCE((
            SELECT SUM(subtotal)
            FROM detalle_venta_productos
            WHERE id_venta = _id_venta
        ),0)
    WHERE id_venta = _id_venta;

    COMMIT;
END $$

CREATE PROCEDURE sp_eliminardetalleproducto(
    IN _id_detalle_producto INT
)
BEGIN
    DECLARE _id_venta INT DEFAULT NULL;
    DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT id_venta
    INTO _id_venta
    FROM detalle_venta_productos
    WHERE id_detalle_producto = _id_detalle_producto
    FOR UPDATE;

    IF _id_venta IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El detalle de venta no existe.';
    END IF;

    SELECT estado
    INTO _estado
    FROM ventas
    WHERE id_venta = _id_venta
    FOR UPDATE;

    IF _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;

    DELETE FROM detalle_venta_productos
    WHERE id_detalle_producto = _id_detalle_producto;

    UPDATE ventas
    SET total =
        COALESCE((
            SELECT SUM(precio_unitario)
            FROM boletos
            WHERE id_venta = _id_venta
              AND estado IN ('reservado','vendido')
        ),0)
        +
        COALESCE((
            SELECT SUM(subtotal)
            FROM detalle_venta_productos
            WHERE id_venta = _id_venta
        ),0)
    WHERE id_venta = _id_venta;

    COMMIT;
END $$

CREATE PROCEDURE sp_listardetallesventa(
    IN _id_venta INT
)
BEGIN
    SELECT
        d.id_detalle_producto,
        d.id_venta,
        d.id_producto,
        p.nombre_producto,
        d.cantidad,
        d.precio_unitario,
        d.subtotal
    FROM detalle_venta_productos d
    INNER JOIN productos p
        ON p.id_producto = d.id_producto
    WHERE d.id_venta = _id_venta
    ORDER BY d.id_detalle_producto;
END $$

-- ============================================================
-- INVENTARIO
-- ============================================================

CREATE PROCEDURE sp_registrarmovimiento(
    IN _id_producto INT,
    IN _id_usuario INT,
    IN _id_venta INT,
    IN _tipo_movimiento VARCHAR(10),
    IN _cantidad INT,
    IN _observacion VARCHAR(255)
)
BEGIN
    DECLARE _stock INT DEFAULT NULL;
    DECLARE _estado_producto TINYINT DEFAULT NULL;
    DECLARE _rol VARCHAR(20) DEFAULT NULL;
    DECLARE _tipo VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SET _tipo = UPPER(TRIM(_tipo_movimiento));

    IF _cantidad IS NULL OR _cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La cantidad debe ser mayor que cero.';
    END IF;

    IF _tipo NOT IN ('ENTRADA','SALIDA','AJUSTE') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tipo de movimiento invalido.';
    END IF;

    IF _observacion IS NULL OR CHAR_LENGTH(TRIM(_observacion)) = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Indicar una observacion.';
    END IF;

    SELECT r.nombre_rol
    INTO _rol
    FROM usuarios u
    INNER JOIN roles r ON r.id_rol = u.id_rol
    WHERE u.id_usuario = _id_usuario
      AND u.estado = 1;

    IF _rol IS NULL OR _rol NOT IN ('admin','bodega') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El movimiento requiere un usuario de bodega o administrador activo.';
    END IF;

    SELECT stock, estado
    INTO _stock, _estado_producto
    FROM productos
    WHERE id_producto = _id_producto
    FOR UPDATE;

    IF _stock IS NULL OR _estado_producto <> 1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El producto no existe o esta inactivo.';
    END IF;

    IF _tipo = 'SALIDA' AND _stock < _cantidad THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Stock insuficiente.';
    END IF;

    IF _tipo = 'ENTRADA' THEN
        UPDATE productos SET stock = stock + _cantidad
        WHERE id_producto = _id_producto;
    ELSEIF _tipo = 'SALIDA' THEN
        UPDATE productos SET stock = stock - _cantidad
        WHERE id_producto = _id_producto;
    ELSE
        UPDATE productos SET stock = _cantidad
        WHERE id_producto = _id_producto;
    END IF;

    INSERT INTO movimientos_inventario(
        id_producto, id_usuario, id_venta,
        tipo_movimiento, cantidad, observacion
    )
    VALUES(
        _id_producto, _id_usuario, _id_venta,
        _tipo, _cantidad, TRIM(_observacion)
    );

    COMMIT;
END $$

CREATE PROCEDURE sp_listarmovimientosinventario()
BEGIN
    SELECT
        m.id_movimiento,
        m.id_producto,
        p.nombre_producto,
        m.id_usuario,
        CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS usuario,
        m.id_venta,
        m.tipo_movimiento,
        m.cantidad,
        m.fecha_movimiento,
        m.observacion
    FROM movimientos_inventario m
    INNER JOIN productos p
        ON p.id_producto = m.id_producto
    INNER JOIN usuarios u
        ON u.id_usuario = m.id_usuario
    ORDER BY m.fecha_movimiento DESC;
END $$

CREATE PROCEDURE sp_listarstockcritico()
BEGIN
    SELECT *
    FROM vw_stock_critico
    ORDER BY stock ASC, nombre_producto;
END $$

-- ============================================================
-- FACTURA
-- ============================================================

CREATE PROCEDURE sp_verfactura(
    IN _id_venta INT
)
BEGIN

    -- Encabezado
    SELECT *
    FROM vw_factura_ventas
    WHERE id_venta = _id_venta;

    -- Boletos
    SELECT
        b.id_boleto,
        p.titulo AS pelicula,
        s.nombre_sala,
        CONCAT(bu.fila, bu.numero) AS butaca,
        f.fecha_inicio,
        f.fecha_fin,
        b.precio_unitario,
        b.estado
    FROM boletos b
    INNER JOIN funciones f
        ON f.id_funcion = b.id_funcion
    INNER JOIN peliculas p
        ON p.id_pelicula = f.id_pelicula
    INNER JOIN salas s
        ON s.id_sala = b.id_sala
    INNER JOIN butacas bu
        ON bu.id_butaca = b.id_butaca
    WHERE b.id_venta = _id_venta;

    -- Productos
    SELECT
        d.id_detalle_producto,
        p.nombre_producto,
        d.cantidad,
        d.precio_unitario,
        d.subtotal
    FROM detalle_venta_productos d
    INNER JOIN productos p
        ON p.id_producto = d.id_producto
    WHERE d.id_venta = _id_venta;
END $$

-- ============================================================
-- DASHBOARD
-- ============================================================

CREATE PROCEDURE sp_obtenerdashboard()
BEGIN
    SELECT
        (
            SELECT COALESCE(SUM(total),0)
            FROM ventas
            WHERE estado = 'confirmada'
        ) AS total_ventas,

        (
            SELECT COUNT(*)
            FROM peliculas
            WHERE estado = 1
        ) AS cantidad_peliculas,

        (
            SELECT COUNT(*)
            FROM usuarios
            WHERE estado = 1
        ) AS usuarios_activos,

        (
            SELECT COUNT(*)
            FROM clientes
            WHERE estado = 1
        ) AS cantidad_clientes,

        (
            SELECT COUNT(*)
            FROM funciones
            WHERE estado = 'programada'
              AND fecha_inicio >= NOW()
        ) AS funciones_activas,

        (
            SELECT COUNT(*)
            FROM vw_stock_critico
        ) AS productos_stock_critico;
END $$

-- ============================================================
-- REPORTES
-- ============================================================

CREATE PROCEDURE sp_reporte_ventas(
    IN _fecha_inicio DATE,
    IN _fecha_fin DATE
)
BEGIN
    SELECT
        v.id_venta,
        v.fecha_venta,
        CONCAT(c.nombre_cliente, ' ', c.apellido_cliente) AS cliente,
        CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS cajero,
        v.total,
        v.metodo_pago,
        v.estado
    FROM ventas v
    INNER JOIN clientes c
        ON c.id_cliente = v.id_cliente
    INNER JOIN usuarios u
        ON u.id_usuario = v.id_usuario
    WHERE DATE(v.fecha_venta)
        BETWEEN _fecha_inicio AND _fecha_fin
    ORDER BY v.fecha_venta DESC;
END $$

-- ============================================================
-- COMPROBACION DE TABLAS
-- ============================================================

CREATE PROCEDURE sp_mostrartablas()
BEGIN
    SELECT TABLE_NAME
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_TYPE = 'BASE TABLE'
    ORDER BY TABLE_NAME;
END $$


-- ============================================================
-- PROCEDIMIENTOS ADICIONALES Y ALIAS DE COMPATIBILIDAD
-- ============================================================

DROP PROCEDURE IF EXISTS sp_registrarmovimientoinventario;
CREATE PROCEDURE sp_registrarmovimientoinventario(
    IN _id_producto INT,
    IN _id_usuario INT,
    IN _tipo_movimiento VARCHAR(20),
    IN _cantidad INT,
    IN _observacion VARCHAR(200)
)
BEGIN
    CALL sp_registrarmovimiento(
        _id_producto,
        _id_usuario,
        NULL,
        UPPER(TRIM(_tipo_movimiento)),
        _cantidad,
        _observacion
    );
END $$

DROP PROCEDURE IF EXISTS sp_listarmovimientosporproducto;
CREATE PROCEDURE sp_listarmovimientosporproducto(IN _id_producto INT)
BEGIN
    IF _id_producto IS NULL OR NOT EXISTS (
        SELECT 1 FROM productos WHERE id_producto = _id_producto
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El producto no existe.';
    END IF;

    SELECT *
    FROM vw_movimientos_inventario
    WHERE id_producto = _id_producto
    ORDER BY fecha_movimiento DESC, id_movimiento DESC;
END $$

DROP PROCEDURE IF EXISTS sp_filtrarmovimientosinventario;
CREATE PROCEDURE sp_filtrarmovimientosinventario(
    IN _id_producto INT,
    IN _fecha_desde DATE,
    IN _fecha_hasta DATE,
    IN _tipo_movimiento VARCHAR(20)
)
BEGIN
    SET _tipo_movimiento = LOWER(TRIM(_tipo_movimiento));

    IF _fecha_desde IS NOT NULL
       AND _fecha_hasta IS NOT NULL
       AND _fecha_desde > _fecha_hasta THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El rango de fechas es invalido.';
    END IF;

    IF _id_producto IS NOT NULL
       AND NOT EXISTS (
           SELECT 1 FROM productos WHERE id_producto = _id_producto
       ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El producto no existe.';
    END IF;

    IF _tipo_movimiento IS NOT NULL
       AND _tipo_movimiento NOT IN ('entrada','salida','ajuste','venta','devolucion') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El tipo de movimiento es invalido.';
    END IF;

    SELECT *
    FROM vw_movimientos_inventario
    WHERE (_id_producto IS NULL OR id_producto = _id_producto)
      AND (_fecha_desde IS NULL OR fecha_movimiento >= _fecha_desde)
      AND (_fecha_hasta IS NULL OR fecha_movimiento < DATE_ADD(_fecha_hasta, INTERVAL 1 DAY))
      AND (
          _tipo_movimiento IS NULL
          OR tipo_movimiento = UPPER(_tipo_movimiento)
          OR (_tipo_movimiento = 'venta' AND tipo_movimiento = 'SALIDA')
          OR (_tipo_movimiento = 'devolucion' AND tipo_movimiento = 'ENTRADA')
      )
    ORDER BY fecha_movimiento DESC, id_movimiento DESC;
END $$

DROP PROCEDURE IF EXISTS sp_listarpeliculasactivas;
CREATE PROCEDURE sp_listarpeliculasactivas()
BEGIN
    SELECT *
    FROM vw_lista_peliculas
    WHERE estado = 1
    ORDER BY titulo, id_pelicula;
END $$

DROP PROCEDURE IF EXISTS sp_listarsalasactivas;
CREATE PROCEDURE sp_listarsalasactivas()
BEGIN
    SELECT *
    FROM vw_lista_salas
    WHERE estado = 1
    ORDER BY nombre_sala, id_sala;
END $$

DROP PROCEDURE IF EXISTS sp_listarproductosactivos;
CREATE PROCEDURE sp_listarproductosactivos()
BEGIN
    SELECT *
    FROM vw_lista_productos
    WHERE estado = 1
    ORDER BY nombre_producto, id_producto;
END $$

DROP PROCEDURE IF EXISTS sp_listarcartelera;
CREATE PROCEDURE sp_listarcartelera()
BEGIN
    SELECT *
    FROM vw_cartelera
    ORDER BY fecha_inicio, nombre_sala;
END $$

DROP PROCEDURE IF EXISTS sp_listarboletosventa;
CREATE PROCEDURE sp_listarboletosventa(IN _id_venta INT)
BEGIN
    SELECT *
    FROM vw_lista_boletos
    WHERE id_venta = _id_venta
    ORDER BY id_boleto;
END $$

DROP PROCEDURE IF EXISTS sp_buscarboleto;
CREATE PROCEDURE sp_buscarboleto(IN _id_boleto INT)
BEGIN
    SELECT *
    FROM vw_lista_boletos
    WHERE id_boleto = _id_boleto;
END $$

DROP PROCEDURE IF EXISTS sp_quitarboleto;
CREATE PROCEDURE sp_quitarboleto(IN _id_venta INT, IN _id_boleto INT)
BEGIN
    DELETE FROM boletos
    WHERE id_boleto = _id_boleto
      AND id_venta = _id_venta
      AND estado = 'reservado';

    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El boleto no pertenece al carrito o ya no esta reservado.';
    END IF;

    UPDATE ventas
    SET total =
        COALESCE((
            SELECT SUM(precio_unitario)
            FROM boletos
            WHERE id_venta = _id_venta
              AND estado IN ('reservado','vendido')
        ),0)
        +
        COALESCE((
            SELECT SUM(subtotal)
            FROM detalle_venta_productos
            WHERE id_venta = _id_venta
        ),0)
    WHERE id_venta = _id_venta
      AND estado = 'abierta';
END $$

DROP PROCEDURE IF EXISTS sp_agregarproductoventa;
CREATE PROCEDURE sp_agregarproductoventa(
    IN _id_venta INT,
    IN _id_producto INT,
    IN _cantidad INT
)
BEGIN
    CALL sp_agregardetalleproducto(_id_venta, _id_producto, _cantidad);
END $$

DROP PROCEDURE IF EXISTS sp_listarproductosventa;
CREATE PROCEDURE sp_listarproductosventa(IN _id_venta INT)
BEGIN
    SELECT
        d.id_detalle_producto,
        d.id_venta,
        d.id_producto,
        p.nombre_producto,
        d.cantidad,
        d.precio_unitario,
        d.subtotal
    FROM detalle_venta_productos d
    INNER JOIN productos p ON p.id_producto = d.id_producto
    WHERE d.id_venta = _id_venta
    ORDER BY d.id_detalle_producto;
END $$

DROP PROCEDURE IF EXISTS sp_actualizarcantidadproducto;
CREATE PROCEDURE sp_actualizarcantidadproducto(
    IN _id_venta INT,
    IN _id_producto INT,
    IN _cantidad INT
)
BEGIN
    DECLARE _detalle INT DEFAULT NULL;

    SELECT id_detalle_producto
    INTO _detalle
    FROM detalle_venta_productos
    WHERE id_venta = _id_venta
      AND id_producto = _id_producto;

    IF _detalle IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El producto no esta en el carrito.';
    END IF;

    CALL sp_actualizardetalleproducto(_detalle, _cantidad);
END $$

DROP PROCEDURE IF EXISTS sp_quitarproductoventa;
CREATE PROCEDURE sp_quitarproductoventa(
    IN _id_venta INT,
    IN _id_producto INT
)
BEGIN
    DECLARE _detalle INT DEFAULT NULL;

    SELECT id_detalle_producto
    INTO _detalle
    FROM detalle_venta_productos
    WHERE id_venta = _id_venta
      AND id_producto = _id_producto;

    IF _detalle IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El producto no esta en el carrito.';
    END IF;

    CALL sp_eliminardetalleproducto(_detalle);
END $$

DROP PROCEDURE IF EXISTS sp_eliminarventaabierta;
CREATE PROCEDURE sp_eliminarventaabierta(IN _id_venta INT)
BEGIN
    DECLARE _estado VARCHAR(20) DEFAULT NULL;

    SELECT estado
    INTO _estado
    FROM ventas
    WHERE id_venta = _id_venta
    FOR UPDATE;

    IF _estado IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;

    IF _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Solo se puede eliminar una venta abierta.';
    END IF;

    DELETE FROM boletos WHERE id_venta = _id_venta;
    DELETE FROM detalle_venta_productos WHERE id_venta = _id_venta;
    DELETE FROM ventas WHERE id_venta = _id_venta;
END $$

DROP PROCEDURE IF EXISTS sp_reporteventas_fechas;
CREATE PROCEDURE sp_reporteventas_fechas(
    IN _fecha_desde DATE,
    IN _fecha_hasta DATE
)
BEGIN
    IF _fecha_desde IS NULL OR _fecha_hasta IS NULL OR _fecha_desde > _fecha_hasta THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Indicar un rango de fechas valido.';
    END IF;

    SELECT *
    FROM vw_lista_ventas
    WHERE estado = 'confirmada'
      AND fecha_venta >= _fecha_desde
      AND fecha_venta < DATE_ADD(_fecha_hasta, INTERVAL 1 DAY)
    ORDER BY fecha_venta DESC, id_venta DESC;
END $$

DROP PROCEDURE IF EXISTS sp_reporteventasdia;
CREATE PROCEDURE sp_reporteventasdia(IN _fecha DATE)
BEGIN
    CALL sp_reporteventas_fechas(_fecha, _fecha);
END $$

DROP PROCEDURE IF EXISTS sp_reporteventassemana;
CREATE PROCEDURE sp_reporteventassemana(IN _fecha DATE)
BEGIN
    DECLARE _lunes DATE;
    SET _lunes = DATE_SUB(_fecha, INTERVAL WEEKDAY(_fecha) DAY);
    CALL sp_reporteventas_fechas(
        _lunes,
        DATE_ADD(_lunes, INTERVAL 6 DAY)
    );
END $$

DROP PROCEDURE IF EXISTS sp_reporteventasmes;
CREATE PROCEDURE sp_reporteventasmes(IN _fecha DATE)
BEGIN
    DECLARE _primero DATE;
    SET _primero = DATE_SUB(
        _fecha,
        INTERVAL (DAYOFMONTH(_fecha) - 1) DAY
    );
    CALL sp_reporteventas_fechas(_primero, LAST_DAY(_fecha));
END $$

DROP PROCEDURE IF EXISTS sp_indicadoresadmin;
CREATE PROCEDURE sp_indicadoresadmin()
BEGIN
    SELECT
        (SELECT COUNT(*) FROM peliculas WHERE estado = 1) AS peliculas_activas,
        (SELECT COUNT(*) FROM clientes WHERE estado = 1) AS clientes_activos,
        (SELECT COUNT(*) FROM usuarios WHERE estado = 1) AS usuarios_activos,
        (
            SELECT COUNT(*)
            FROM funciones
            WHERE estado = 'programada'
              AND fecha_inicio > NOW()
        ) AS funciones_pendientes,
        (SELECT COUNT(*) FROM vw_stock_critico) AS productos_stock_critico,
        (SELECT COUNT(*) FROM ventas WHERE estado = 'confirmada') AS ventas_confirmadas,
        (SELECT COALESCE(SUM(total),0)
         FROM ventas
         WHERE estado = 'confirmada') AS ventas_totales;
END $$

DELIMITER ;

-- ============================================================
-- COMPROBACIONES RAPIDAS
-- ============================================================
-- CALL sp_listarpeliculasactivas();
-- CALL sp_listarsalasactivas();
-- CALL sp_listarfunciones();
-- CALL sp_listarcartelera();
-- CALL sp_listarmovimientosinventario();
-- CALL sp_listarstockcritico();
-- CALL sp_listarventas();
-- CALL sp_mostrartablas();

-- ============================================================
-- FIN DE PROCEDIMIENTOS
-- ============================================================
