-- CINE: SP CORREGIDO 1419 - CORREO EN PROCEDIMIENTOS
-- ============================================================
-- ORDEN OFICIAL DE EJECUCION
-- 1) 01_DDL_Cine.sql
-- 2) 02_DML_Cine.sql
-- 3) 03_SP_Vistas_Cine.sql
--
-- NOTA: El DDL recrea la base de datos desde cero.
-- No ejecutar el DDL sobre una base con datos que se quieran conservar.
-- ============================================================

-- ============================================================
-- 03_SP_Vistas_Cine.sql
-- Correo validado en registro y edición. Stock crítico: stock <= stock_minimo.
-- Vistas y procedimientos almacenados
-- Compatible con cinedb_in4cm
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
DROP PROCEDURE IF EXISTS sp_listarboletosventa;

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

DROP PROCEDURE IF EXISTS sp_agregarproductoventa;
DROP PROCEDURE IF EXISTS sp_actualizarcantidadproducto;
DROP PROCEDURE IF EXISTS sp_quitarproductoventa;
DROP PROCEDURE IF EXISTS sp_listarproductosventa;
DROP PROCEDURE IF EXISTS sp_listarproductosdisponibles;
DROP PROCEDURE IF EXISTS sp_validarstockventa;
DROP PROCEDURE IF EXISTS sp_registrarmovimientoinventario;

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
    SET _correo_electronico = LOWER(TRIM(_correo_electronico));
    IF NOT (COALESCE(CHAR_LENGTH(_correo_electronico) BETWEEN 3 AND 120 AND CHAR_LENGTH(SUBSTRING_INDEX(_correo_electronico, '@', 1)) <= 64 AND REGEXP_LIKE(_correo_electronico, '^[A-Za-z0-9_%+-]+([.][A-Za-z0-9_%+-]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?([.][A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*[.][A-Za-z]{2,63}$', 'c'), 0) = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Correo inválido: use nombre@dominio.com, sin espacios ni puntos vacíos.';
    END IF;
    IF EXISTS (SELECT 1 FROM usuarios WHERE correo_electronico = _correo_electronico) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El correo ya está registrado.';
    END IF;
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
    SET _correo_electronico = LOWER(TRIM(_correo_electronico));
    IF NOT (COALESCE(CHAR_LENGTH(_correo_electronico) BETWEEN 3 AND 120 AND CHAR_LENGTH(SUBSTRING_INDEX(_correo_electronico, '@', 1)) <= 64 AND REGEXP_LIKE(_correo_electronico, '^[A-Za-z0-9_%+-]+([.][A-Za-z0-9_%+-]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?([.][A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*[.][A-Za-z]{2,63}$', 'c'), 0) = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Correo inválido: use nombre@dominio.com, sin espacios ni puntos vacíos.';
    END IF;
    IF EXISTS (SELECT 1 FROM usuarios WHERE correo_electronico = _correo_electronico AND id_usuario <> _id_usuario) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El correo ya está registrado.';
    END IF;
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
    SET _correo_electronico = LOWER(TRIM(_correo_electronico));
    IF NOT (COALESCE(CHAR_LENGTH(_correo_electronico) BETWEEN 3 AND 120 AND CHAR_LENGTH(SUBSTRING_INDEX(_correo_electronico, '@', 1)) <= 64 AND REGEXP_LIKE(_correo_electronico, '^[A-Za-z0-9_%+-]+([.][A-Za-z0-9_%+-]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?([.][A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*[.][A-Za-z]{2,63}$', 'c'), 0) = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Correo inválido: use nombre@dominio.com, sin espacios ni puntos vacíos.';
    END IF;
    IF EXISTS (SELECT 1 FROM clientes WHERE correo_electronico = _correo_electronico) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El correo ya está registrado.';
    END IF;
    INSERT INTO clientes (
        cui, nombre_cliente, apellido_cliente, nit,
        correo_electronico, telefono, estado
    )
    VALUES (
        _cui, TRIM(_nombre_cliente), TRIM(_apellido_cliente),
        COALESCE(NULLIF(TRIM(_nit), ''), 'CF'),
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
    SET _correo_electronico = LOWER(TRIM(_correo_electronico));
    IF NOT (COALESCE(CHAR_LENGTH(_correo_electronico) BETWEEN 3 AND 120 AND CHAR_LENGTH(SUBSTRING_INDEX(_correo_electronico, '@', 1)) <= 64 AND REGEXP_LIKE(_correo_electronico, '^[A-Za-z0-9_%+-]+([.][A-Za-z0-9_%+-]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?([.][A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*[.][A-Za-z]{2,63}$', 'c'), 0) = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Correo inválido: use nombre@dominio.com, sin espacios ni puntos vacíos.';
    END IF;
    IF EXISTS (SELECT 1 FROM clientes WHERE correo_electronico = _correo_electronico AND id_cliente <> _id_cliente) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El correo ya está registrado.';
    END IF;
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

-- ============================================================
-- PELICULAS
-- IMPORTANTE:
-- INSERT: 9 parametros
-- 1 titulo
-- 2 sinopsis
-- 3 director
-- 4 duracion
-- 5 clasificacion
-- 6 idioma
-- 7 fecha
-- 8 imagen
-- 9 id_genero
--
-- UPDATE: 9 parametros
-- 1 id
-- 2 titulo
-- 3 sinopsis
-- 4 director
-- 5 duracion
-- 6 clasificacion
-- 7 idioma
-- 8 fecha
-- 9 id_genero
--
-- Esto coincide con el PeliculaDAOImpl actual.
-- ============================================================

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
    INSERT INTO butacas(id_sala, fila, numero, estado)
    VALUES(_id_sala, UPPER(TRIM(_fila)), _numero, 1);

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
    UPDATE butacas
    SET
        id_sala = _id_sala,
        fila = UPPER(TRIM(_fila)),
        numero = _numero
    WHERE id_butaca = _id_butaca;
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
    DECLARE _duracion INT;
    DECLARE _fecha_fin DATETIME;

    SELECT duracion_minutos
    INTO _duracion
    FROM peliculas
    WHERE id_pelicula = _id_pelicula
      AND estado = 1;

    IF _duracion IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La pelicula no existe o esta inactiva.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM salas
        WHERE id_sala = _id_sala AND estado = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La sala no existe o esta inactiva.';
    END IF;

    IF _fecha_inicio <= NOW() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La funcion debe programarse para una fecha futura.';
    END IF;

    SET _fecha_fin = DATE_ADD(_fecha_inicio, INTERVAL (_duracion + 20) MINUTE);

    IF EXISTS (
        SELECT 1
        FROM funciones
        WHERE id_sala = _id_sala
          AND estado = 'programada'
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

    SELECT LAST_INSERT_ID() AS id_funcion;
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
    DECLARE _duracion INT;
    DECLARE _fecha_fin DATETIME;

    SELECT duracion_minutos
    INTO _duracion
    FROM peliculas
    WHERE id_pelicula = _id_pelicula
      AND estado = 1;

    IF _duracion IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La pelicula no existe o esta inactiva.';
    END IF;

    SET _fecha_fin = DATE_ADD(_fecha_inicio, INTERVAL (_duracion + 20) MINUTE);

    IF EXISTS (
        SELECT 1
        FROM funciones
        WHERE id_sala = _id_sala
          AND id_funcion <> _id_funcion
          AND estado = 'programada'
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
    WHERE id_funcion = _id_funcion
      AND estado = 'programada';
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
    IF EXISTS (
        SELECT 1
        FROM boletos
        WHERE id_funcion = _id_funcion
          AND estado IN ('reservado','vendido')
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'No se puede cancelar una funcion que tiene boletos activos.';
    END IF;

    UPDATE funciones
    SET estado = 'cancelada'
    WHERE id_funcion = _id_funcion
      AND estado = 'programada';
END $$

CREATE PROCEDURE sp_finalizarfuncion(
    IN _id_funcion INT
)
BEGIN
    UPDATE funciones
    SET estado = 'finalizada'
    WHERE id_funcion = _id_funcion
      AND estado = 'programada'
      AND fecha_fin <= NOW();
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
CREATE PROCEDURE sp_listarboletosventa(
    IN _id_venta INT
)
BEGIN
    SELECT
        id_boleto,
        id_venta,
        id_funcion,
        id_pelicula,
        titulo,
        id_sala,
        nombre_sala,
        id_butaca,
        fila,
        numero,
        butaca,
        fecha_inicio,
        fecha_fin,
        precio_unitario,
        estado
    FROM vw_lista_boletos
    WHERE id_venta = _id_venta
      AND estado IN ('reservado', 'vendido')
    ORDER BY fila, numero, id_boleto;
END $$

CREATE PROCEDURE sp_abrirventa(IN _id_cliente INT, IN _id_usuario INT)
BEGIN
IF NOT EXISTS (SELECT 1 FROM clientes WHERE id_cliente = _id_cliente AND estado = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seleccione un cliente activo.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol
        WHERE u.id_usuario = _id_usuario AND u.estado = 1 AND r.nombre_rol IN ('admin','taquillero')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Se requiere un administrador o taquillero activo.';
    END IF;
    INSERT INTO ventas(id_cliente, id_usuario, total, metodo_pago, estado)
    VALUES (_id_cliente, _id_usuario, 0, 'EFECTIVO', 'abierta');
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

CREATE PROCEDURE sp_actualizarclienteventa(IN _id_venta INT, IN _id_cliente INT)
BEGIN
DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT estado INTO _estado FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado IS NULL OR _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya no está abierta.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM clientes WHERE id_cliente = _id_cliente AND estado = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seleccione un cliente activo.';
    END IF;
    UPDATE ventas SET id_cliente = _id_cliente WHERE id_venta = _id_venta;
    COMMIT;
END $$

CREATE PROCEDURE sp_agregarboleto(IN _id_venta INT, IN _id_funcion INT, IN _id_butaca INT)
BEGIN
DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE _sala INT DEFAULT NULL;
    DECLARE _precio DECIMAL(10,2);
    DECLARE _inicio DATETIME;
    DECLARE _estado_funcion VARCHAR(20);
    DECLARE _pelicula_activa TINYINT;
    DECLARE _sala_activa TINYINT;
    DECLARE _boleto INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT estado INTO _estado FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado IS NULL OR _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya no está abierta.';
    END IF;

    SELECT f.id_sala, f.precio_boleto, f.fecha_inicio, f.estado, p.estado, s.estado
    INTO _sala, _precio, _inicio, _estado_funcion, _pelicula_activa, _sala_activa
    FROM funciones f JOIN peliculas p ON p.id_pelicula = f.id_pelicula
    JOIN salas s ON s.id_sala = f.id_sala WHERE f.id_funcion = _id_funcion FOR UPDATE;
    IF _sala IS NULL OR _estado_funcion <> 'programada' OR _inicio <= NOW()
        OR _pelicula_activa <> 1 OR _sala_activa <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La función no está disponible para la venta.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM butacas WHERE id_butaca = _id_butaca AND id_sala = _sala AND estado = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La butaca no está activa o pertenece a otra sala.';
    END IF;
    IF EXISTS (SELECT 1 FROM boletos WHERE id_funcion = _id_funcion AND id_butaca = _id_butaca AND estado IN ('reservado','vendido')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La butaca ya está ocupada para esta función.';
    END IF;
    INSERT INTO boletos(id_venta, id_funcion, id_butaca, id_sala, precio_unitario, estado)
    VALUES (_id_venta, _id_funcion, _id_butaca, _sala, _precio, 'reservado');
    SET _boleto = LAST_INSERT_ID();
    UPDATE ventas v SET v.total =
        (SELECT COALESCE(SUM(precio_unitario), 0) FROM boletos WHERE id_venta = _id_venta AND estado IN ('reservado','vendido'))
        + (SELECT COALESCE(SUM(subtotal), 0) FROM detalle_venta_productos WHERE id_venta = _id_venta)
    WHERE v.id_venta = _id_venta;
    COMMIT;
    SELECT _boleto AS id_boleto;
END $$

CREATE PROCEDURE sp_eliminarboleto(IN _id_boleto INT)
BEGIN
DECLARE _id_venta INT DEFAULT NULL;
    DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT id_venta INTO _id_venta FROM boletos WHERE id_boleto = _id_boleto;
    IF _id_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El boleto no existe.';
    END IF;
    SELECT estado INTO _estado FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado IS NULL OR _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya no está abierta.';
    END IF;

    DELETE FROM boletos WHERE id_boleto = _id_boleto AND estado = 'reservado';
    IF ROW_COUNT() <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se pueden quitar boletos reservados.';
    END IF;
    UPDATE ventas v SET v.total =
        (SELECT COALESCE(SUM(precio_unitario), 0) FROM boletos WHERE id_venta = _id_venta AND estado IN ('reservado','vendido'))
        + (SELECT COALESCE(SUM(subtotal), 0) FROM detalle_venta_productos WHERE id_venta = _id_venta)
    WHERE v.id_venta = _id_venta;
    COMMIT;
END $$

CREATE PROCEDURE sp_confirmarventa(IN _id_venta INT, IN _metodo_pago VARCHAR(30))
BEGIN
DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE _id_usuario INT;
    DECLARE _id_cliente INT;
    DECLARE _pago VARCHAR(30);
    DECLARE _fin TINYINT DEFAULT 0;
    DECLARE _id_producto INT;
    DECLARE _cantidad BIGINT;
    DECLARE _stock INT DEFAULT NULL;
    DECLARE _activo TINYINT;
    DECLARE productos_venta CURSOR FOR SELECT id_producto, SUM(cantidad)
        FROM detalle_venta_productos WHERE id_venta = _id_venta GROUP BY id_producto ORDER BY id_producto;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET _fin = 1;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET _pago = UPPER(TRIM(COALESCE(_metodo_pago, '')));
    IF _pago NOT IN ('EFECTIVO','TARJETA','TRANSFERENCIA') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seleccione un método de pago válido.';
    END IF;
    START TRANSACTION;
    SELECT estado, id_usuario, id_cliente INTO _estado, _id_usuario, _id_cliente
    FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado IS NULL OR _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya no está abierta.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM clientes WHERE id_cliente = _id_cliente AND estado = 1)
        OR NOT EXISTS (SELECT 1 FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol
            WHERE u.id_usuario = _id_usuario AND u.estado = 1 AND r.nombre_rol IN ('admin','taquillero')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El cliente o vendedor de la venta ya no está activo.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM boletos WHERE id_venta = _id_venta AND estado = 'reservado')
        AND NOT EXISTS (SELECT 1 FROM detalle_venta_productos WHERE id_venta = _id_venta) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede confirmar una venta vacía.';
    END IF;
    -- Bloqueo y revalidación de funciones: evita confirmar reservas canceladas o vencidas.
    BEGIN
        DECLARE _terminado TINYINT DEFAULT 0;
        DECLARE _funcion INT;
        DECLARE _inicio DATETIME;
        DECLARE _estado_f VARCHAR(20);
        DECLARE _pelicula_a TINYINT;
        DECLARE _sala_a TINYINT;
        DECLARE funciones_venta CURSOR FOR SELECT DISTINCT id_funcion FROM boletos
            WHERE id_venta = _id_venta AND estado = 'reservado' ORDER BY id_funcion;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET _terminado = 1;
        OPEN funciones_venta;
        funciones_loop: LOOP
            FETCH funciones_venta INTO _funcion;
            IF _terminado = 1 THEN LEAVE funciones_loop; END IF;
            SELECT f.fecha_inicio, f.estado, p.estado, s.estado
            INTO _inicio, _estado_f, _pelicula_a, _sala_a
            FROM funciones f JOIN peliculas p ON p.id_pelicula = f.id_pelicula
            JOIN salas s ON s.id_sala = f.id_sala WHERE f.id_funcion = _funcion FOR UPDATE;
            IF _estado_f <> 'programada' OR _inicio <= NOW() OR _pelicula_a <> 1 OR _sala_a <> 1 THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Una función ya no está disponible para confirmar.';
            END IF;
        END LOOP;
        CLOSE funciones_venta;
    END;
    SET _fin = 0;
    OPEN productos_venta;
    productos_loop: LOOP
        FETCH productos_venta INTO _id_producto, _cantidad;
        IF _fin = 1 THEN LEAVE productos_loop; END IF;
        SELECT stock, estado INTO _stock, _activo FROM productos WHERE id_producto = _id_producto FOR UPDATE;
        IF _stock IS NULL OR _activo <> 1 OR _stock < _cantidad THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuficiente o producto inactivo al confirmar.';
        END IF;
        UPDATE productos SET stock = stock - _cantidad WHERE id_producto = _id_producto;
        INSERT INTO movimientos_inventario(id_producto, id_usuario, id_venta, tipo_movimiento, cantidad, observacion)
        VALUES (_id_producto, _id_usuario, _id_venta, 'SALIDA', _cantidad, CONCAT('Venta #', _id_venta));
    END LOOP;
    CLOSE productos_venta;
    UPDATE boletos SET estado = 'vendido' WHERE id_venta = _id_venta AND estado = 'reservado';
    UPDATE ventas v SET v.total =
        (SELECT COALESCE(SUM(precio_unitario), 0) FROM boletos WHERE id_venta = _id_venta AND estado IN ('reservado','vendido'))
        + (SELECT COALESCE(SUM(subtotal), 0) FROM detalle_venta_productos WHERE id_venta = _id_venta)
    WHERE v.id_venta = _id_venta;

    UPDATE ventas SET estado = 'confirmada', metodo_pago = _pago, fecha_confirmacion = NOW()
    WHERE id_venta = _id_venta;
    COMMIT;
    SELECT * FROM vw_lista_ventas WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_anularventa(IN _id_venta INT, IN _motivo VARCHAR(200))
BEGIN
DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE _id_usuario INT;
    DECLARE _fin TINYINT DEFAULT 0;
    DECLARE _id_producto INT;
    DECLARE _cantidad BIGINT;
    DECLARE _stock INT DEFAULT NULL;
    DECLARE _activo TINYINT;
    DECLARE productos_venta CURSOR FOR SELECT id_producto, SUM(cantidad)
        FROM detalle_venta_productos WHERE id_venta = _id_venta GROUP BY id_producto ORDER BY id_producto;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET _fin = 1;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF _motivo IS NULL OR CHAR_LENGTH(TRIM(_motivo)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Indique el motivo de anulación.';
    END IF;
    START TRANSACTION;
    SELECT estado, id_usuario INTO _estado, _id_usuario FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado IS NULL OR _estado = 'anulada' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya está anulada.';
    END IF;
    IF _estado = 'confirmada' THEN
        SET _fin = 0;
        OPEN productos_venta;
        productos_loop: LOOP
            FETCH productos_venta INTO _id_producto, _cantidad;
            IF _fin = 1 THEN LEAVE productos_loop; END IF;
            SELECT stock INTO _stock FROM productos WHERE id_producto = _id_producto FOR UPDATE;
            UPDATE productos SET stock = stock + _cantidad WHERE id_producto = _id_producto;
            INSERT INTO movimientos_inventario(id_producto, id_usuario, id_venta, tipo_movimiento, cantidad, observacion)
            VALUES (_id_producto, _id_usuario, _id_venta, 'ENTRADA', _cantidad, CONCAT('Devolución de venta #', _id_venta));
        END LOOP;
        CLOSE productos_venta;
    END IF;
    UPDATE boletos SET estado = 'anulado' WHERE id_venta = _id_venta AND estado IN ('reservado','vendido');
    UPDATE ventas SET estado = 'anulada', fecha_anulacion = NOW(), motivo_anulacion = TRIM(_motivo)
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

CREATE PROCEDURE sp_agregardetalleproducto(IN _id_venta INT, IN _id_producto INT, IN _cantidad INT)
BEGIN
CALL sp_agregarproductoventa(_id_venta, _id_producto, _cantidad);
END $$

CREATE PROCEDURE sp_actualizardetalleproducto(IN _id_detalle_producto INT, IN _cantidad INT)
BEGIN
DECLARE _id_venta INT DEFAULT NULL;
    DECLARE _id_producto INT DEFAULT NULL;
    SELECT id_venta, id_producto INTO _id_venta, _id_producto FROM detalle_venta_productos
    WHERE id_detalle_producto = _id_detalle_producto;
    IF _id_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La línea de producto no existe.';
    END IF;
    CALL sp_actualizarcantidadproducto(_id_venta, _id_producto, _cantidad);
END $$

CREATE PROCEDURE sp_eliminardetalleproducto(IN _id_detalle_producto INT)
BEGIN
DECLARE _id_venta INT DEFAULT NULL;
    DECLARE _id_producto INT DEFAULT NULL;
    SELECT id_venta, id_producto INTO _id_venta, _id_producto FROM detalle_venta_productos
    WHERE id_detalle_producto = _id_detalle_producto;
    IF _id_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La línea de producto no existe.';
    END IF;
    CALL sp_quitarproductoventa(_id_venta, _id_producto);
END $$

CREATE PROCEDURE sp_listardetallesventa(IN _id_venta INT)
BEGIN
CALL sp_listarproductosventa(_id_venta);
END $$

-- ============================================================
-- INVENTARIO
-- ============================================================

CREATE PROCEDURE sp_registrarmovimiento(IN _id_producto INT, IN _id_usuario INT, IN _id_venta INT, IN _tipo_movimiento VARCHAR(20), IN _cantidad INT, IN _observacion VARCHAR(255))
BEGIN
DECLARE _stock INT DEFAULT NULL;
    DECLARE _tipo VARCHAR(20);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET _tipo = UPPER(TRIM(COALESCE(_tipo_movimiento, '')));
    IF _tipo NOT IN ('ENTRADA','SALIDA','AJUSTE') OR _cantidad IS NULL OR _cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tipo de movimiento o cantidad inválidos.';
    END IF;
    IF _observacion IS NULL OR CHAR_LENGTH(TRIM(_observacion)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Indique la observación del movimiento.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol
        WHERE u.id_usuario = _id_usuario AND u.estado = 1 AND r.nombre_rol IN ('admin','bodega')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El movimiento requiere un administrador o encargado de bodega activo.';
    END IF;
    IF _id_venta IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Los movimientos vinculados a ventas se generan al confirmar o anular.';
    END IF;
    START TRANSACTION;
    SELECT stock INTO _stock FROM productos WHERE id_producto = _id_producto FOR UPDATE;
    IF _stock IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no existe.'; END IF;
    IF _tipo = 'SALIDA' AND _cantidad > _stock THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuficiente para la salida de inventario.';
    END IF;
    UPDATE productos SET stock = CASE _tipo WHEN 'ENTRADA' THEN stock + _cantidad
        WHEN 'SALIDA' THEN stock - _cantidad ELSE _cantidad END WHERE id_producto = _id_producto;
    INSERT INTO movimientos_inventario(id_producto, id_usuario, id_venta, tipo_movimiento, cantidad, observacion)
    VALUES (_id_producto, _id_usuario, NULL, _tipo, _cantidad, TRIM(_observacion));
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
SELECT * FROM vw_stock_critico ORDER BY stock, stock_minimo, nombre_producto;
END $$

-- ============================================================
-- FACTURA
-- ============================================================

CREATE PROCEDURE sp_verfactura(
    IN _id_venta INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ventas WHERE id_venta = _id_venta AND estado = 'confirmada') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La factura requiere una venta confirmada.';
    END IF;

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
    WHERE b.id_venta = _id_venta AND b.estado = 'vendido' ORDER BY b.id_boleto;

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
    WHERE d.id_venta = _id_venta ORDER BY d.id_detalle_producto;
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

-- Consultas usadas por el flujo de venta de dulcería en Java.
CREATE PROCEDURE sp_agregarproductoventa(IN _id_venta INT, IN _id_producto INT, IN _cantidad INT)
BEGIN
DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE _stock INT DEFAULT NULL;
    DECLARE _activo TINYINT DEFAULT 0;
    DECLARE _precio DECIMAL(10,2);
    DECLARE _actual BIGINT DEFAULT 0;
    DECLARE _nueva BIGINT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    IF _cantidad IS NULL OR _cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad debe ser mayor que cero.';
    END IF;
    START TRANSACTION;
    SELECT estado INTO _estado FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado IS NULL OR _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya no está abierta.';
    END IF;
    SELECT stock, estado, precio INTO _stock, _activo, _precio
    FROM productos WHERE id_producto = _id_producto FOR UPDATE;
    IF _stock IS NULL OR _activo <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no existe o está inactivo.';
    END IF;
    SELECT COALESCE(SUM(cantidad), 0) INTO _actual FROM detalle_venta_productos
    WHERE id_venta = _id_venta AND id_producto = _id_producto;
    SET _nueva = _actual + _cantidad;
    IF _nueva > _stock THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuficiente para la cantidad acumulada.';
    END IF;
    IF _actual = 0 THEN
        INSERT INTO detalle_venta_productos(id_venta, id_producto, cantidad, precio_unitario, subtotal)
        VALUES (_id_venta, _id_producto, _nueva, _precio, _nueva * _precio);
    ELSE
        UPDATE detalle_venta_productos SET cantidad = _nueva, subtotal = _nueva * precio_unitario
        WHERE id_venta = _id_venta AND id_producto = _id_producto;
    END IF;
    UPDATE ventas v SET v.total =
        (SELECT COALESCE(SUM(precio_unitario), 0) FROM boletos WHERE id_venta = _id_venta AND estado IN ('reservado','vendido'))
        + (SELECT COALESCE(SUM(subtotal), 0) FROM detalle_venta_productos WHERE id_venta = _id_venta)
    WHERE v.id_venta = _id_venta;
    COMMIT;
    SELECT id_venta, total FROM ventas WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_actualizarcantidadproducto(IN _id_venta INT, IN _id_producto INT, IN _cantidad INT)
BEGIN
DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE _stock INT DEFAULT NULL;
    DECLARE _activo TINYINT DEFAULT 0;
    DECLARE _precio DECIMAL(10,2);
    DECLARE _actual BIGINT DEFAULT 0;
    DECLARE _nueva BIGINT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    IF _cantidad IS NULL OR _cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad debe ser mayor que cero.';
    END IF;
    START TRANSACTION;
    SELECT estado INTO _estado FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado IS NULL OR _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya no está abierta.';
    END IF;
    SELECT stock, estado, precio INTO _stock, _activo, _precio
    FROM productos WHERE id_producto = _id_producto FOR UPDATE;
    IF _stock IS NULL OR _activo <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no existe o está inactivo.';
    END IF;
    SELECT COALESCE(SUM(cantidad), 0) INTO _actual FROM detalle_venta_productos
    WHERE id_venta = _id_venta AND id_producto = _id_producto;
    IF _actual = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no pertenece a esta venta.';
    END IF;
    SET _nueva = _cantidad;
    IF _nueva > _stock THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuficiente para la cantidad acumulada.';
    END IF;
    IF _actual = 0 THEN
        INSERT INTO detalle_venta_productos(id_venta, id_producto, cantidad, precio_unitario, subtotal)
        VALUES (_id_venta, _id_producto, _nueva, _precio, _nueva * _precio);
    ELSE
        UPDATE detalle_venta_productos SET cantidad = _nueva, subtotal = _nueva * precio_unitario
        WHERE id_venta = _id_venta AND id_producto = _id_producto;
    END IF;
    UPDATE ventas v SET v.total =
        (SELECT COALESCE(SUM(precio_unitario), 0) FROM boletos WHERE id_venta = _id_venta AND estado IN ('reservado','vendido'))
        + (SELECT COALESCE(SUM(subtotal), 0) FROM detalle_venta_productos WHERE id_venta = _id_venta)
    WHERE v.id_venta = _id_venta;
    COMMIT;
    SELECT id_venta, total FROM ventas WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_quitarproductoventa(IN _id_venta INT, IN _id_producto INT)
BEGIN
DECLARE _estado VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT estado INTO _estado FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado IS NULL OR _estado <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya no está abierta.';
    END IF;

    DELETE FROM detalle_venta_productos WHERE id_venta = _id_venta AND id_producto = _id_producto;
    UPDATE ventas v SET v.total =
        (SELECT COALESCE(SUM(precio_unitario), 0) FROM boletos WHERE id_venta = _id_venta AND estado IN ('reservado','vendido'))
        + (SELECT COALESCE(SUM(subtotal), 0) FROM detalle_venta_productos WHERE id_venta = _id_venta)
    WHERE v.id_venta = _id_venta;
    COMMIT;
END $$

CREATE PROCEDURE sp_listarproductosventa(IN _id_venta INT)
BEGIN
SELECT d.id_detalle_producto, d.id_venta, d.id_producto, p.nombre_producto,
        d.cantidad, d.precio_unitario, d.subtotal
    FROM detalle_venta_productos d JOIN productos p ON p.id_producto = d.id_producto
    WHERE d.id_venta = _id_venta ORDER BY d.id_detalle_producto;
END $$

CREATE PROCEDURE sp_listarproductosdisponibles()
BEGIN
SELECT * FROM vw_lista_productos WHERE estado = 1 ORDER BY nombre_producto, id_producto;
END $$

CREATE PROCEDURE sp_validarstockventa(IN _id_venta INT)
BEGIN
SELECT d.id_producto, p.nombre_producto, SUM(d.cantidad) AS cantidad, p.stock, p.estado
    FROM detalle_venta_productos d JOIN productos p ON p.id_producto = d.id_producto
    WHERE d.id_venta = _id_venta
    GROUP BY d.id_producto, p.nombre_producto, p.stock, p.estado
    HAVING SUM(d.cantidad) > p.stock OR p.estado <> 1
    ORDER BY d.id_producto;
END $$

CREATE PROCEDURE sp_registrarmovimientoinventario(IN _id_producto INT, IN _id_usuario INT, IN _tipo_movimiento VARCHAR(20), IN _cantidad INT, IN _observacion VARCHAR(200))
BEGIN
CALL sp_registrarmovimiento(_id_producto, _id_usuario, NULL, _tipo_movimiento, _cantidad, _observacion);
END $$

DELIMITER ;

-- ============================================================
-- FIN DE PROCEDIMIENTOS
-- ============================================================
