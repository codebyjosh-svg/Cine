-- PROYECTO CINE: ESTRUCTURA, PROCEDIMIENTOS Y VISTAS
-- Adaptación completa del script DDL proporcionado.
-- US-1.2: validaciones del CRUD de usuarios integradas con el login de US-1.1.
-- Servidor: MySQL 8.0.16 o posterior. Motor InnoDB y codificación UTF-8.
-- 1. Ejecutar este archivo una sola vez en una base de datos nueva.
-- 2. Ejecutar después cinedb_script_dml.sql.
-- Este script crea cinedb_in4cm; los datos de la base anterior se conservan.
-- No contiene rutas de imágenes ni archivos multimedia.
-- Las claves con historial usan RESTRICT: desactivar si no se pueden eliminar.
-- Los procedimientos de ventas/inventario administran su propia transacción:
-- invocarlos desde Java sin una transacción externa que ya tenga cambios.
-- La sesión y los permisos del usuario se verifican además en la aplicación.

SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS cinedb_in4cm
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cinedb_in4cm;

-- 1. CLIENTES Y ACCESO AL SISTEMA
CREATE TABLE clientes (
    id_cliente INT PRIMARY KEY AUTO_INCREMENT,
    cui CHAR(13) NULL UNIQUE,
    nombre_cliente VARCHAR(100) NOT NULL,
    apellido_cliente VARCHAR(100) NOT NULL,
    correo_electronico VARCHAR(120) NOT NULL UNIQUE,
    telefono VARCHAR(20) NULL,
    estado TINYINT NOT NULL DEFAULT 1,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cliente_nombre CHECK (CHAR_LENGTH(TRIM(nombre_cliente)) > 0),
    CONSTRAINT chk_cliente_apellido CHECK (CHAR_LENGTH(TRIM(apellido_cliente)) > 0),
    CONSTRAINT chk_cliente_cui CHECK (cui IS NULL OR (CHAR_LENGTH(cui) = 13 AND cui NOT REGEXP '[^0-9]')),
    CONSTRAINT chk_cliente_correo CHECK (
        correo_electronico LIKE '_%@_%._%' AND correo_electronico NOT LIKE '% %'
        AND correo_electronico NOT LIKE '%@%@%' AND correo_electronico NOT LIKE '%.@%'
        AND correo_electronico NOT LIKE '%..%'),
    CONSTRAINT chk_cliente_estado CHECK (estado IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE roles (
    id_rol INT PRIMARY KEY AUTO_INCREMENT,
    nombre_rol VARCHAR(20) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NOT NULL,
    CONSTRAINT chk_rol_nombre CHECK (nombre_rol IN ('admin', 'taquillero', 'bodega', 'cliente'))
) ENGINE=InnoDB;

CREATE TABLE usuarios (
    id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    nombre_usuario VARCHAR(100) NOT NULL,
    apellido_usuario VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    correo_electronico VARCHAR(120) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    id_cliente INT NULL UNIQUE,
    estado TINYINT NOT NULL DEFAULT 1,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE RESTRICT,
    CONSTRAINT fk_usuario_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente) ON DELETE RESTRICT,
    CONSTRAINT chk_usuario_nombre CHECK (CHAR_LENGTH(TRIM(nombre_usuario)) > 0),
    CONSTRAINT chk_usuario_apellido CHECK (CHAR_LENGTH(TRIM(apellido_usuario)) > 0),
    CONSTRAINT chk_usuario_username CHECK (CHAR_LENGTH(TRIM(username)) >= 3 AND username NOT LIKE '% %'),
    CONSTRAINT chk_usuario_hash CHECK (CHAR_LENGTH(contrasena_hash) >= 60),
    CONSTRAINT chk_usuario_correo CHECK (
        correo_electronico LIKE '_%@_%._%' AND correo_electronico NOT LIKE '% %'
        AND correo_electronico NOT LIKE '%@%@%' AND correo_electronico NOT LIKE '%.@%'
        AND correo_electronico NOT LIKE '%..%'),
    CONSTRAINT chk_usuario_estado CHECK (estado IN (0, 1))
) ENGINE=InnoDB;

-- 2. CATÁLOGO DE PELÍCULAS
CREATE TABLE generos (
    id_genero INT PRIMARY KEY AUTO_INCREMENT,
    nombre_genero VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NULL,
    estado TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT chk_genero_nombre CHECK (CHAR_LENGTH(TRIM(nombre_genero)) > 0),
    CONSTRAINT chk_genero_estado CHECK (estado IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE peliculas (
    id_pelicula INT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(150) NOT NULL,
    sinopsis TEXT NULL,
    director VARCHAR(150) NULL,
    duracion_minutos INT NOT NULL,
    clasificacion VARCHAR(20) NOT NULL,
    idioma VARCHAR(50) NOT NULL,
    fecha_estreno DATE NULL,
    id_genero INT NOT NULL,
    estado TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_pelicula_genero FOREIGN KEY (id_genero) REFERENCES generos(id_genero) ON DELETE RESTRICT,
    CONSTRAINT chk_pelicula_titulo CHECK (CHAR_LENGTH(TRIM(titulo)) > 0),
    CONSTRAINT chk_pelicula_duracion CHECK (duracion_minutos BETWEEN 1 AND 600),
    CONSTRAINT chk_pelicula_clasificacion CHECK (CHAR_LENGTH(TRIM(clasificacion)) > 0),
    CONSTRAINT chk_pelicula_idioma CHECK (CHAR_LENGTH(TRIM(idioma)) > 0),
    CONSTRAINT chk_pelicula_estado CHECK (estado IN (0, 1))
) ENGINE=InnoDB;

-- 3. SALAS, BUTACAS Y FUNCIONES
-- La capacidad se obtiene contando las butacas activas de cada sala.
CREATE TABLE salas (
    id_sala INT PRIMARY KEY AUTO_INCREMENT,
    nombre_sala VARCHAR(100) NOT NULL UNIQUE,
    formato VARCHAR(20) NOT NULL DEFAULT '2D',
    estado TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT chk_sala_nombre CHECK (CHAR_LENGTH(TRIM(nombre_sala)) > 0),
    CONSTRAINT chk_sala_formato CHECK (formato IN ('2D', '3D')),
    CONSTRAINT chk_sala_estado CHECK (estado IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE butacas (
    id_butaca INT PRIMARY KEY AUTO_INCREMENT,
    id_sala INT NOT NULL,
    fila VARCHAR(3) NOT NULL,
    numero INT NOT NULL,
    estado TINYINT NOT NULL DEFAULT 1,
    UNIQUE KEY uq_butaca_ubicacion (id_sala, fila, numero),
    UNIQUE KEY uq_butaca_sala (id_butaca, id_sala),
    CONSTRAINT fk_butaca_sala FOREIGN KEY (id_sala) REFERENCES salas(id_sala) ON DELETE RESTRICT,
    CONSTRAINT chk_butaca_fila CHECK (CHAR_LENGTH(TRIM(fila)) > 0),
    CONSTRAINT chk_butaca_numero CHECK (numero > 0),
    CONSTRAINT chk_butaca_estado CHECK (estado IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE funciones (
    id_funcion INT PRIMARY KEY AUTO_INCREMENT,
    id_pelicula INT NOT NULL,
    id_sala INT NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    precio_boleto DECIMAL(10,2) NOT NULL,
    estado ENUM('programada', 'finalizada', 'cancelada') NOT NULL DEFAULT 'programada',
    UNIQUE KEY uq_funcion_sala (id_funcion, id_sala),
    KEY idx_funcion_horario (id_sala, estado, fecha_inicio, fecha_fin),
    CONSTRAINT fk_funcion_pelicula FOREIGN KEY (id_pelicula) REFERENCES peliculas(id_pelicula) ON DELETE RESTRICT,
    CONSTRAINT fk_funcion_sala FOREIGN KEY (id_sala) REFERENCES salas(id_sala) ON DELETE RESTRICT,
    CONSTRAINT chk_funcion_fechas CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT chk_funcion_precio CHECK (precio_boleto > 0)
) ENGINE=InnoDB;

-- 4. CONFITERÍA E INVENTARIO
CREATE TABLE categorias_producto (
    id_categoria_producto INT PRIMARY KEY AUTO_INCREMENT,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NULL,
    estado TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT chk_categoria_producto_nombre CHECK (CHAR_LENGTH(TRIM(nombre_categoria)) > 0),
    CONSTRAINT chk_categoria_producto_estado CHECK (estado IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE productos (
    id_producto INT PRIMARY KEY AUTO_INCREMENT,
    nombre_producto VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    id_categoria_producto INT NOT NULL,
    estado TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria_producto) REFERENCES categorias_producto(id_categoria_producto) ON DELETE RESTRICT,
    CONSTRAINT chk_producto_nombre CHECK (CHAR_LENGTH(TRIM(nombre_producto)) > 0),
    CONSTRAINT chk_producto_precio CHECK (precio > 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0),
    CONSTRAINT chk_producto_minimo CHECK (stock_minimo >= 0),
    CONSTRAINT chk_producto_estado CHECK (estado IN (0, 1))
) ENGINE=InnoDB;

-- 5. VENTAS DE BOLETOS Y PRODUCTOS
-- El cliente es obligatorio. El total se calcula en vw_lista_ventas;
-- no se recibe un total escrito desde Java.
CREATE TABLE ventas (
    id_venta INT PRIMARY KEY AUTO_INCREMENT,
    fecha_venta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_cliente INT NOT NULL,
    id_usuario INT NOT NULL,
    estado ENUM('abierta', 'confirmada', 'anulada') NOT NULL DEFAULT 'abierta',
    fecha_confirmacion DATETIME NULL,
    fecha_anulacion DATETIME NULL,
    motivo_anulacion VARCHAR(200) NULL,
    KEY idx_venta_fecha (estado, fecha_venta),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente) ON DELETE RESTRICT,
    CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE boletos (
    id_boleto INT PRIMARY KEY AUTO_INCREMENT,
    id_venta INT NOT NULL,
    id_funcion INT NOT NULL,
    id_butaca INT NOT NULL,
    id_sala INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    estado ENUM('reservado', 'vendido', 'anulado') NOT NULL DEFAULT 'reservado',
    -- NULL permite conservar varios boletos anulados de la misma butaca.
    -- La clave UNIQUE impide dos reservas/ventas activas para la misma función.
    id_funcion_ocupada INT GENERATED ALWAYS AS
        (CASE WHEN estado IN ('reservado', 'vendido') THEN id_funcion ELSE NULL END) STORED,
    UNIQUE KEY uq_boleto_butaca_funcion (id_funcion_ocupada, id_butaca),
    CONSTRAINT fk_boleto_venta FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE RESTRICT,
    CONSTRAINT fk_boleto_funcion_sala FOREIGN KEY (id_funcion, id_sala) REFERENCES funciones(id_funcion, id_sala) ON DELETE RESTRICT,
    CONSTRAINT fk_boleto_butaca_sala FOREIGN KEY (id_butaca, id_sala) REFERENCES butacas(id_butaca, id_sala) ON DELETE RESTRICT,
    CONSTRAINT chk_boleto_precio CHECK (precio_unitario > 0)
) ENGINE=InnoDB;

CREATE TABLE detalle_venta_productos (
    id_detalle_producto INT PRIMARY KEY AUTO_INCREMENT,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    UNIQUE KEY uq_detalle_venta_producto (id_venta, id_producto),
    CONSTRAINT fk_detalle_producto_venta FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE RESTRICT,
    CONSTRAINT fk_detalle_producto_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE RESTRICT,
    CONSTRAINT chk_detalle_producto_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_producto_precio CHECK (precio_unitario > 0)
) ENGINE=InnoDB;

CREATE TABLE movimientos_inventario (
    id_movimiento INT PRIMARY KEY AUTO_INCREMENT,
    id_producto INT NOT NULL,
    id_usuario INT NOT NULL,
    id_venta INT NULL,
    tipo_movimiento ENUM('entrada', 'salida', 'venta', 'devolucion') NOT NULL,
    cantidad INT NOT NULL,
    fecha_movimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion VARCHAR(200) NOT NULL,
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE RESTRICT,
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_movimiento_venta FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE RESTRICT,
    CONSTRAINT chk_movimiento_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_movimiento_observacion CHECK (CHAR_LENGTH(TRIM(observacion)) > 0)
) ENGINE=InnoDB;

-- PROCEDIMIENTOS CRUD
-- Los catálogos tienen agregar, listar, buscar, editar, eliminar y cambiar estado.
-- 1 = activo; 0 = inactivo. Las eliminaciones físicas respetan las relaciones.
DELIMITER $$

CREATE PROCEDURE sp_insertarcliente(
    IN _cui CHAR(13),
    IN _nombre_cliente VARCHAR(100),
    IN _apellido_cliente VARCHAR(100),
    IN _correo_electronico VARCHAR(120),
    IN _telefono VARCHAR(20)
)
BEGIN
    INSERT INTO clientes (cui, nombre_cliente, apellido_cliente, correo_electronico, telefono)
    VALUES (_cui, _nombre_cliente, _apellido_cliente, _correo_electronico, _telefono);
    SELECT LAST_INSERT_ID() AS id_cliente;
END $$

CREATE PROCEDURE sp_listarclientes(

)
BEGIN
    SELECT id_cliente, cui, nombre_cliente, apellido_cliente, correo_electronico, telefono, fecha_registro, estado FROM clientes ORDER BY id_cliente;
END $$

CREATE PROCEDURE sp_buscarcliente(
    IN _id_cliente INT
)
BEGIN
    SELECT id_cliente, cui, nombre_cliente, apellido_cliente, correo_electronico, telefono, fecha_registro, estado FROM clientes WHERE id_cliente = _id_cliente;
END $$

CREATE PROCEDURE sp_actualizarcliente(
    IN _id_cliente INT,
    IN _cui CHAR(13),
    IN _nombre_cliente VARCHAR(100),
    IN _apellido_cliente VARCHAR(100),
    IN _correo_electronico VARCHAR(120),
    IN _telefono VARCHAR(20)
)
BEGIN
    UPDATE clientes SET
        cui = _cui,
        nombre_cliente = _nombre_cliente,
        apellido_cliente = _apellido_cliente,
        correo_electronico = _correo_electronico,
        telefono = _telefono
    WHERE id_cliente = _id_cliente;
END $$

CREATE PROCEDURE sp_eliminarcliente(
    IN _id_cliente INT
)
BEGIN
    DELETE FROM clientes WHERE id_cliente = _id_cliente;
END $$

CREATE PROCEDURE sp_cambiarestadocliente(
    IN _id_cliente INT,
    IN _estado TINYINT
)
BEGIN
    UPDATE clientes SET estado = _estado WHERE id_cliente = _id_cliente;
END $$

CREATE PROCEDURE sp_insertargenero(
    IN _nombre_genero VARCHAR(100),
    IN _descripcion VARCHAR(200)
)
BEGIN
    INSERT INTO generos (nombre_genero, descripcion)
    VALUES (_nombre_genero, _descripcion);
    SELECT LAST_INSERT_ID() AS id_genero;
END $$

CREATE PROCEDURE sp_listargeneros(

)
BEGIN
    SELECT id_genero, nombre_genero, descripcion, estado FROM generos ORDER BY id_genero;
END $$

CREATE PROCEDURE sp_buscargenero(
    IN _id_genero INT
)
BEGIN
    SELECT id_genero, nombre_genero, descripcion, estado FROM generos WHERE id_genero = _id_genero;
END $$

CREATE PROCEDURE sp_actualizargenero(
    IN _id_genero INT,
    IN _nombre_genero VARCHAR(100),
    IN _descripcion VARCHAR(200)
)
BEGIN
    UPDATE generos SET
        nombre_genero = _nombre_genero,
        descripcion = _descripcion
    WHERE id_genero = _id_genero;
END $$

CREATE PROCEDURE sp_eliminargenero(
    IN _id_genero INT
)
BEGIN
    DELETE FROM generos WHERE id_genero = _id_genero;
END $$

CREATE PROCEDURE sp_cambiarestadogenero(
    IN _id_genero INT,
    IN _estado TINYINT
)
BEGIN
    UPDATE generos SET estado = _estado WHERE id_genero = _id_genero;
END $$

CREATE PROCEDURE sp_insertarpelicula(
    IN _titulo VARCHAR(150),
    IN _sinopsis TEXT,
    IN _director VARCHAR(150),
    IN _duracion_minutos INT,
    IN _clasificacion VARCHAR(20),
    IN _idioma VARCHAR(50),
    IN _fecha_estreno DATE,
    IN _id_genero INT
)
BEGIN
    INSERT INTO peliculas (titulo, sinopsis, director, duracion_minutos, clasificacion, idioma, fecha_estreno, id_genero)
    VALUES (_titulo, _sinopsis, _director, _duracion_minutos, _clasificacion, _idioma, _fecha_estreno, _id_genero);
    SELECT LAST_INSERT_ID() AS id_pelicula;
END $$

CREATE PROCEDURE sp_listarpeliculas(

)
BEGIN
    SELECT id_pelicula, titulo, sinopsis, director, duracion_minutos, clasificacion, idioma, fecha_estreno, id_genero, estado FROM peliculas ORDER BY id_pelicula;
END $$

CREATE PROCEDURE sp_buscarpelicula(
    IN _id_pelicula INT
)
BEGIN
    SELECT id_pelicula, titulo, sinopsis, director, duracion_minutos, clasificacion, idioma, fecha_estreno, id_genero, estado FROM peliculas WHERE id_pelicula = _id_pelicula;
END $$

CREATE PROCEDURE sp_actualizarpelicula(
    IN _id_pelicula INT,
    IN _titulo VARCHAR(150),
    IN _sinopsis TEXT,
    IN _director VARCHAR(150),
    IN _duracion_minutos INT,
    IN _clasificacion VARCHAR(20),
    IN _idioma VARCHAR(50),
    IN _fecha_estreno DATE,
    IN _id_genero INT
)
BEGIN
    IF EXISTS (SELECT 1 FROM peliculas p JOIN funciones f ON f.id_pelicula = p.id_pelicula
        WHERE p.id_pelicula = _id_pelicula AND p.duracion_minutos <> _duracion_minutos
        AND f.estado = 'programada' AND f.fecha_fin > NOW()) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Reprogramar las funciones antes de cambiar la duración.';
    END IF;
    UPDATE peliculas SET
        titulo = _titulo,
        sinopsis = _sinopsis,
        director = _director,
        duracion_minutos = _duracion_minutos,
        clasificacion = _clasificacion,
        idioma = _idioma,
        fecha_estreno = _fecha_estreno,
        id_genero = _id_genero
    WHERE id_pelicula = _id_pelicula;
END $$

CREATE PROCEDURE sp_eliminarpelicula(
    IN _id_pelicula INT
)
BEGIN
    DELETE FROM peliculas WHERE id_pelicula = _id_pelicula;
END $$

CREATE PROCEDURE sp_cambiarestadopelicula(
    IN _id_pelicula INT,
    IN _estado TINYINT
)
BEGIN
    IF _estado = 0 AND EXISTS (SELECT 1 FROM funciones WHERE id_pelicula = _id_pelicula
        AND estado = 'programada' AND fecha_fin > NOW()) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cancelar o finalizar las funciones antes de desactivar la película.';
    END IF;
    UPDATE peliculas SET estado = _estado WHERE id_pelicula = _id_pelicula;
END $$

CREATE PROCEDURE sp_insertarsala(
    IN _nombre_sala VARCHAR(100),
    IN _formato VARCHAR(20)
)
BEGIN
    INSERT INTO salas (nombre_sala, formato)
    VALUES (_nombre_sala, _formato);
    SELECT LAST_INSERT_ID() AS id_sala;
END $$

CREATE PROCEDURE sp_listarsalas(

)
BEGIN
    SELECT id_sala, nombre_sala, formato, estado FROM salas ORDER BY id_sala;
END $$

CREATE PROCEDURE sp_buscarsala(
    IN _id_sala INT
)
BEGIN
    SELECT id_sala, nombre_sala, formato, estado FROM salas WHERE id_sala = _id_sala;
END $$

CREATE PROCEDURE sp_actualizarsala(
    IN _id_sala INT,
    IN _nombre_sala VARCHAR(100),
    IN _formato VARCHAR(20)
)
BEGIN
    UPDATE salas SET
        nombre_sala = _nombre_sala,
        formato = _formato
    WHERE id_sala = _id_sala;
END $$

CREATE PROCEDURE sp_eliminarsala(
    IN _id_sala INT
)
BEGIN
    DELETE FROM salas WHERE id_sala = _id_sala;
END $$

CREATE PROCEDURE sp_cambiarestadosala(
    IN _id_sala INT,
    IN _estado TINYINT
)
BEGIN
    IF _estado = 0 AND EXISTS (SELECT 1 FROM funciones WHERE id_sala = _id_sala
        AND estado = 'programada' AND fecha_fin > NOW()) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cancelar o finalizar las funciones antes de desactivar la sala.';
    END IF;
    UPDATE salas SET estado = _estado WHERE id_sala = _id_sala;
END $$

CREATE PROCEDURE sp_insertarcategoriaproducto(
    IN _nombre_categoria VARCHAR(100),
    IN _descripcion VARCHAR(200)
)
BEGIN
    INSERT INTO categorias_producto (nombre_categoria, descripcion)
    VALUES (_nombre_categoria, _descripcion);
    SELECT LAST_INSERT_ID() AS id_categoria_producto;
END $$

CREATE PROCEDURE sp_listarcategorias_producto(

)
BEGIN
    SELECT id_categoria_producto, nombre_categoria, descripcion, estado FROM categorias_producto ORDER BY id_categoria_producto;
END $$

CREATE PROCEDURE sp_buscarcategoriaproducto(
    IN _id_categoria_producto INT
)
BEGIN
    SELECT id_categoria_producto, nombre_categoria, descripcion, estado FROM categorias_producto WHERE id_categoria_producto = _id_categoria_producto;
END $$

CREATE PROCEDURE sp_actualizarcategoriaproducto(
    IN _id_categoria_producto INT,
    IN _nombre_categoria VARCHAR(100),
    IN _descripcion VARCHAR(200)
)
BEGIN
    UPDATE categorias_producto SET
        nombre_categoria = _nombre_categoria,
        descripcion = _descripcion
    WHERE id_categoria_producto = _id_categoria_producto;
END $$

CREATE PROCEDURE sp_eliminarcategoriaproducto(
    IN _id_categoria_producto INT
)
BEGIN
    DELETE FROM categorias_producto WHERE id_categoria_producto = _id_categoria_producto;
END $$

CREATE PROCEDURE sp_cambiarestadocategoriaproducto(
    IN _id_categoria_producto INT,
    IN _estado TINYINT
)
BEGIN
    UPDATE categorias_producto SET estado = _estado WHERE id_categoria_producto = _id_categoria_producto;
END $$

CREATE PROCEDURE sp_insertarproducto(
    IN _nombre_producto VARCHAR(100),
    IN _descripcion VARCHAR(200),
    IN _precio DECIMAL(10,2),
    IN _stock_minimo INT,
    IN _id_categoria_producto INT
)
BEGIN
    INSERT INTO productos (nombre_producto, descripcion, precio, stock_minimo, id_categoria_producto)
    VALUES (_nombre_producto, _descripcion, _precio, _stock_minimo, _id_categoria_producto);
    SELECT LAST_INSERT_ID() AS id_producto;
END $$

CREATE PROCEDURE sp_listarproductos(

)
BEGIN
    SELECT id_producto, nombre_producto, descripcion, precio, stock_minimo, id_categoria_producto, stock, estado FROM productos ORDER BY id_producto;
END $$

CREATE PROCEDURE sp_buscarproducto(
    IN _id_producto INT
)
BEGIN
    SELECT id_producto, nombre_producto, descripcion, precio, stock_minimo, id_categoria_producto, stock, estado FROM productos WHERE id_producto = _id_producto;
END $$

CREATE PROCEDURE sp_actualizarproducto(
    IN _id_producto INT,
    IN _nombre_producto VARCHAR(100),
    IN _descripcion VARCHAR(200),
    IN _precio DECIMAL(10,2),
    IN _stock_minimo INT,
    IN _id_categoria_producto INT
)
BEGIN
    UPDATE productos SET
        nombre_producto = _nombre_producto,
        descripcion = _descripcion,
        precio = _precio,
        stock_minimo = _stock_minimo,
        id_categoria_producto = _id_categoria_producto
    WHERE id_producto = _id_producto;
END $$

CREATE PROCEDURE sp_eliminarproducto(
    IN _id_producto INT
)
BEGIN
    DELETE FROM productos WHERE id_producto = _id_producto;
END $$

CREATE PROCEDURE sp_cambiarestadoproducto(
    IN _id_producto INT,
    IN _estado TINYINT
)
BEGIN
    UPDATE productos SET estado = _estado WHERE id_producto = _id_producto;
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
    DECLARE _rol VARCHAR(20) DEFAULT NULL;
    SELECT nombre_rol INTO _rol FROM roles WHERE id_rol = _id_rol;
    IF _rol IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El rol seleccionado no existe.';
    END IF;
    IF _rol = 'cliente' AND (_id_cliente IS NULL OR NOT EXISTS (
        SELECT 1 FROM clientes WHERE id_cliente = _id_cliente AND estado = 1
    )) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seleccionar un cliente activo para vincular la cuenta.';
    END IF;
    IF _rol <> 'cliente' AND _id_cliente IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo una cuenta de rol cliente puede vincularse a un cliente.';
    END IF;
    INSERT INTO usuarios (nombre_usuario, apellido_usuario, username, correo_electronico, id_rol, id_cliente, contrasena_hash)
    VALUES (TRIM(_nombre_usuario), TRIM(_apellido_usuario), TRIM(_username), TRIM(_correo_electronico),
        _id_rol, _id_cliente, _contrasena_hash);
    SELECT LAST_INSERT_ID() AS id_usuario;
END $$

CREATE PROCEDURE sp_listarusuarios(

)
BEGIN
    SELECT u.id_usuario, u.nombre_usuario, u.apellido_usuario, u.username,
        u.correo_electronico, u.id_rol, r.nombre_rol, u.id_cliente, u.estado, u.fecha_registro
    FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol ORDER BY u.id_usuario;
END $$

CREATE PROCEDURE sp_buscarusuario(
    IN _id_usuario INT
)
BEGIN
    SELECT u.id_usuario, u.nombre_usuario, u.apellido_usuario, u.username,
        u.correo_electronico, u.id_rol, r.nombre_rol, u.id_cliente, u.estado, u.fecha_registro
    FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol WHERE u.id_usuario = _id_usuario;
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
    DECLARE _rol VARCHAR(20) DEFAULT NULL;
    DECLARE _cliente_anterior INT DEFAULT NULL;
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = _id_usuario) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El usuario ya no existe.';
    END IF;
    SELECT id_cliente INTO _cliente_anterior FROM usuarios WHERE id_usuario = _id_usuario;
    SELECT nombre_rol INTO _rol FROM roles WHERE id_rol = _id_rol;
    IF _rol IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El rol seleccionado no existe.';
    END IF;
    IF _rol = 'cliente' AND (_id_cliente IS NULL OR NOT EXISTS (
        SELECT 1 FROM clientes WHERE id_cliente = _id_cliente
            AND (estado = 1 OR _id_cliente <=> _cliente_anterior)
    )) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seleccionar un cliente activo para vincular la cuenta.';
    END IF;
    IF _rol <> 'cliente' AND _id_cliente IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo una cuenta de rol cliente puede vincularse a un cliente.';
    END IF;
    UPDATE usuarios SET nombre_usuario = TRIM(_nombre_usuario), apellido_usuario = TRIM(_apellido_usuario),
        username = TRIM(_username), correo_electronico = TRIM(_correo_electronico), id_rol = _id_rol,
        id_cliente = _id_cliente WHERE id_usuario = _id_usuario;
END $$

CREATE PROCEDURE sp_eliminarusuario(IN _id_usuario INT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = _id_usuario) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El usuario ya no existe.';
    END IF;
    DELETE FROM usuarios WHERE id_usuario = _id_usuario;
END $$

CREATE PROCEDURE sp_cambiarestadousuario(IN _id_usuario INT, IN _estado TINYINT)
BEGIN
    IF _estado IS NULL OR _estado NOT IN (0, 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El estado debe ser 0 o 1.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = _id_usuario) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El usuario ya no existe.';
    END IF;
    UPDATE usuarios SET estado = _estado WHERE id_usuario = _id_usuario;
END $$

CREATE PROCEDURE sp_cambiarcontrasena(IN _id_usuario INT, IN _contrasena_hash VARCHAR(255))
BEGIN
    IF _contrasena_hash IS NULL OR CHAR_LENGTH(_contrasena_hash) < 60 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La contraseña debe enviarse como hash seguro.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = _id_usuario) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El usuario ya no existe.';
    END IF;
    UPDATE usuarios SET contrasena_hash = _contrasena_hash WHERE id_usuario = _id_usuario;
END $$

CREATE PROCEDURE sp_buscarusuario_login(
    IN _username VARCHAR(50)
)
BEGIN
    SELECT u.id_usuario, u.nombre_usuario, u.apellido_usuario, u.username,
        u.correo_electronico, u.contrasena_hash, u.id_rol, r.nombre_rol,
        u.id_cliente, u.estado, u.fecha_registro
    FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol
    WHERE u.username = TRIM(_username);
END $$

CREATE PROCEDURE sp_listarroles(

)
BEGIN
    SELECT id_rol, nombre_rol, descripcion FROM roles ORDER BY id_rol;
END $$

CREATE PROCEDURE sp_listarclientesactivos(

)
BEGIN
    SELECT id_cliente, cui, nombre_cliente, apellido_cliente, correo_electronico, telefono
    FROM clientes WHERE estado = 1 ORDER BY apellido_cliente, nombre_cliente;
END $$

CREATE PROCEDURE sp_insertarbutaca(
    IN _id_sala INT,
    IN _fila VARCHAR(3),
    IN _numero INT
)
BEGIN
    INSERT INTO butacas (id_sala, fila, numero) VALUES (_id_sala, UPPER(TRIM(_fila)), _numero);
    SELECT LAST_INSERT_ID() AS id_butaca;
END $$

CREATE PROCEDURE sp_listarbutacas(

)
BEGIN
    SELECT id_butaca, id_sala, fila, numero, estado FROM butacas ORDER BY id_sala, fila, numero;
END $$

CREATE PROCEDURE sp_buscarbutaca(
    IN _id_butaca INT
)
BEGIN
    SELECT id_butaca, id_sala, fila, numero, estado FROM butacas WHERE id_butaca = _id_butaca;
END $$

CREATE PROCEDURE sp_actualizarbutaca(
    IN _id_butaca INT,
    IN _id_sala INT,
    IN _fila VARCHAR(3),
    IN _numero INT
)
BEGIN
    DECLARE _existe INT DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT id_butaca INTO _existe FROM butacas WHERE id_butaca = _id_butaca FOR UPDATE;
    IF _existe IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La butaca no existe.'; END IF;
    IF EXISTS (SELECT 1 FROM boletos WHERE id_butaca = _id_butaca) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Una butaca con historial no puede cambiar de ubicación.';
    END IF;
    UPDATE butacas SET id_sala = _id_sala, fila = UPPER(TRIM(_fila)), numero = _numero
    WHERE id_butaca = _id_butaca;
    COMMIT;
END $$

CREATE PROCEDURE sp_eliminarbutaca(
    IN _id_butaca INT
)
BEGIN
    DELETE FROM butacas WHERE id_butaca = _id_butaca;
END $$

CREATE PROCEDURE sp_cambiarestadobutaca(
    IN _id_butaca INT,
    IN _estado TINYINT
)
BEGIN
    DECLARE _existe INT DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT id_butaca INTO _existe FROM butacas WHERE id_butaca = _id_butaca FOR UPDATE;
    IF _existe IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La butaca no existe.'; END IF;
    IF _estado = 0 AND EXISTS (SELECT 1 FROM boletos b JOIN funciones f ON f.id_funcion = b.id_funcion
        WHERE b.id_butaca = _id_butaca AND b.estado IN ('reservado', 'vendido')
        AND f.estado = 'programada' AND f.fecha_fin > NOW()) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La butaca tiene boletos activos para una función pendiente.';
    END IF;
    UPDATE butacas SET estado = _estado WHERE id_butaca = _id_butaca;
    COMMIT;
END $$

CREATE PROCEDURE sp_insertarfuncion(
    IN _id_pelicula INT,
    IN _id_sala INT,
    IN _fecha_inicio DATETIME,
    IN _precio_boleto DECIMAL(10,2)
)
BEGIN
    DECLARE _sala_activa TINYINT DEFAULT NULL;
    DECLARE _pelicula_activa TINYINT DEFAULT NULL;
    DECLARE _duracion INT DEFAULT NULL;
    DECLARE _fecha_fin DATETIME;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _sala_activa FROM salas WHERE id_sala = _id_sala FOR UPDATE;
    SELECT duracion_minutos, estado INTO _duracion, _pelicula_activa
    FROM peliculas WHERE id_pelicula = _id_pelicula FOR UPDATE;
    IF _sala_activa IS NULL OR _sala_activa <> 1 OR _pelicula_activa IS NULL OR _pelicula_activa <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seleccionar una película y una sala activas.';
    END IF;
    IF _fecha_inicio IS NULL OR _fecha_inicio <= NOW() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La función debe programarse para una fecha futura.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM butacas WHERE id_sala = _id_sala AND estado = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La sala debe tener butacas activas.';
    END IF;
    SET _duracion = _duracion + 20;
    SET _fecha_fin = DATE_ADD(_fecha_inicio, INTERVAL _duracion MINUTE);
    IF EXISTS (SELECT 1 FROM funciones WHERE id_sala = _id_sala AND estado <> 'cancelada'
        AND _fecha_inicio < fecha_fin AND _fecha_fin > fecha_inicio) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El horario se cruza con otra función de la sala.';
    END IF;
    INSERT INTO funciones (id_pelicula, id_sala, fecha_inicio, fecha_fin, precio_boleto)
    VALUES (_id_pelicula, _id_sala, _fecha_inicio, _fecha_fin, _precio_boleto);
    COMMIT;
    SELECT LAST_INSERT_ID() AS id_funcion;
END $$

CREATE PROCEDURE sp_listarfunciones(

)
BEGIN
    SELECT * FROM vw_lista_funciones ORDER BY fecha_inicio, id_sala;
END $$

CREATE PROCEDURE sp_buscarfuncion(
    IN _id_funcion INT
)
BEGIN
    SELECT * FROM vw_lista_funciones WHERE id_funcion = _id_funcion;
END $$

CREATE PROCEDURE sp_actualizarfuncion(
    IN _id_funcion INT,
    IN _id_pelicula INT,
    IN _id_sala INT,
    IN _fecha_inicio DATETIME,
    IN _precio_boleto DECIMAL(10,2)
)
BEGIN
    DECLARE _sala_activa TINYINT DEFAULT NULL;
    DECLARE _pelicula_activa TINYINT DEFAULT NULL;
    DECLARE _duracion INT DEFAULT NULL;
    DECLARE _fecha_fin DATETIME;
    DECLARE _estado_funcion VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _sala_activa FROM salas WHERE id_sala = _id_sala FOR UPDATE;
    SELECT duracion_minutos, estado INTO _duracion, _pelicula_activa
    FROM peliculas WHERE id_pelicula = _id_pelicula FOR UPDATE;
    IF _sala_activa IS NULL OR _sala_activa <> 1 OR _pelicula_activa IS NULL OR _pelicula_activa <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seleccionar una película y una sala activas.';
    END IF;
    IF _fecha_inicio IS NULL OR _fecha_inicio <= NOW() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La función debe programarse para una fecha futura.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM butacas WHERE id_sala = _id_sala AND estado = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La sala debe tener butacas activas.';
    END IF;
    SET _duracion = _duracion + 20;
    SET _fecha_fin = DATE_ADD(_fecha_inicio, INTERVAL _duracion MINUTE);
    SELECT estado INTO _estado_funcion FROM funciones WHERE id_funcion = _id_funcion FOR UPDATE;
    IF _estado_funcion IS NULL OR _estado_funcion <> 'programada' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede editar una función programada.';
    END IF;
    IF EXISTS (SELECT 1 FROM boletos WHERE id_funcion = _id_funcion) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La función tiene historial de boletos; crear otra función.';
    END IF;
    IF EXISTS (SELECT 1 FROM funciones WHERE id_sala = _id_sala AND id_funcion <> _id_funcion
        AND estado <> 'cancelada' AND _fecha_inicio < fecha_fin AND _fecha_fin > fecha_inicio) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El horario se cruza con otra función de la sala.';
    END IF;
    UPDATE funciones SET id_pelicula = _id_pelicula, id_sala = _id_sala, fecha_inicio = _fecha_inicio,
        fecha_fin = _fecha_fin, precio_boleto = _precio_boleto WHERE id_funcion = _id_funcion;
    COMMIT;
END $$

CREATE PROCEDURE sp_eliminarfuncion(
    IN _id_funcion INT
)
BEGIN
    DELETE FROM funciones WHERE id_funcion = _id_funcion;
END $$

CREATE PROCEDURE sp_cancelarfuncion(
    IN _id_funcion INT
)
BEGIN
    DECLARE _estado_funcion VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_funcion FROM funciones WHERE id_funcion = _id_funcion FOR UPDATE;
    IF _estado_funcion IS NULL OR _estado_funcion <> 'programada' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede cancelar una función programada.';
    END IF;
    IF EXISTS (SELECT 1 FROM boletos WHERE id_funcion = _id_funcion AND estado IN ('reservado','vendido')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Anular primero las ventas que tengan boletos de esta función.';
    END IF;
    UPDATE funciones SET estado = 'cancelada' WHERE id_funcion = _id_funcion;
    COMMIT;
END $$

CREATE PROCEDURE sp_finalizarfuncion(
    IN _id_funcion INT
)
BEGIN
    UPDATE funciones SET estado = 'finalizada'
    WHERE id_funcion = _id_funcion AND estado = 'programada' AND fecha_fin <= NOW();
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La función no existe, ya terminó o su horario aún no finaliza.';
    END IF;
END $$

CREATE PROCEDURE sp_listarbutacasfuncion(
    IN _id_funcion INT
)
BEGIN
    SELECT b.id_butaca, b.id_sala, b.fila, b.numero, b.estado,
        CASE WHEN b.estado = 0 THEN 'inactiva'
             WHEN EXISTS (SELECT 1 FROM boletos bo WHERE bo.id_funcion = f.id_funcion
                AND bo.id_butaca = b.id_butaca AND bo.estado IN ('reservado','vendido')) THEN 'ocupada'
             ELSE 'disponible' END AS disponibilidad
    FROM funciones f JOIN butacas b ON b.id_sala = f.id_sala
    WHERE f.id_funcion = _id_funcion ORDER BY b.fila, b.numero;
END $$

CREATE PROCEDURE sp_registrarmovimientoinventario(
    IN _id_producto INT,
    IN _id_usuario INT,
    IN _tipo_movimiento VARCHAR(20),
    IN _cantidad INT,
    IN _observacion VARCHAR(200)
)
BEGIN
    DECLARE _stock INT DEFAULT NULL;
    DECLARE _activo TINYINT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    IF _cantidad IS NULL OR _cantidad <= 0 OR _tipo_movimiento IS NULL
        OR _tipo_movimiento NOT IN ('entrada','salida') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Usar entrada/salida y una cantidad mayor que cero.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol
        WHERE u.id_usuario = _id_usuario AND u.estado = 1 AND r.nombre_rol IN ('admin','bodega')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El movimiento requiere un usuario de bodega o administrador activo.';
    END IF;
    SELECT stock, estado INTO _stock, _activo FROM productos WHERE id_producto = _id_producto FOR UPDATE;
    IF _stock IS NULL OR _activo <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no existe o está inactivo.';
    END IF;
    IF _tipo_movimiento = 'salida' AND _stock < _cantidad THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No hay stock suficiente para la salida.';
    END IF;
    UPDATE productos SET stock = stock + CASE WHEN _tipo_movimiento = 'entrada' THEN _cantidad ELSE -_cantidad END
    WHERE id_producto = _id_producto;
    INSERT INTO movimientos_inventario (id_producto, id_usuario, tipo_movimiento, cantidad, observacion)
    VALUES (_id_producto, _id_usuario, _tipo_movimiento, _cantidad, _observacion);
    COMMIT;
END $$

CREATE PROCEDURE sp_listarmovimientosinventario(

)
BEGIN
    SELECT * FROM vw_movimientos_inventario ORDER BY id_movimiento DESC;
END $$

CREATE PROCEDURE sp_listarstockcritico(

)
BEGIN
    SELECT * FROM vw_stock_critico ORDER BY stock, nombre_producto;
END $$

CREATE PROCEDURE sp_abrirventa(
    IN _id_cliente INT,
    IN _id_usuario INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM clientes WHERE id_cliente = _id_cliente AND estado = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seleccionar un cliente activo antes de crear la venta.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol
        WHERE u.id_usuario = _id_usuario AND u.estado = 1 AND r.nombre_rol IN ('admin','taquillero')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta requiere un taquillero o administrador activo.';
    END IF;
    INSERT INTO ventas (id_cliente, id_usuario) VALUES (_id_cliente, _id_usuario);
    SELECT LAST_INSERT_ID() AS id_venta;
END $$

CREATE PROCEDURE sp_listarventas(

)
BEGIN
    SELECT * FROM vw_lista_ventas ORDER BY fecha_venta DESC, id_venta DESC;
END $$

CREATE PROCEDURE sp_buscarventa(
    IN _id_venta INT
)
BEGIN
    SELECT * FROM vw_lista_ventas WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_actualizarclienteventa(
    IN _id_venta INT,
    IN _id_cliente INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_venta FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;
    IF _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM clientes WHERE id_cliente = _id_cliente AND estado = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seleccionar un cliente activo.';
    END IF;
    UPDATE ventas SET id_cliente = _id_cliente WHERE id_venta = _id_venta;
    COMMIT;
END $$

CREATE PROCEDURE sp_agregarboleto(
    IN _id_venta INT,
    IN _id_funcion INT,
    IN _id_butaca INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE _sala_funcion INT DEFAULT NULL;
    DECLARE _sala_butaca INT DEFAULT NULL;
    DECLARE _butaca_activa TINYINT;
    DECLARE _precio DECIMAL(10,2);
    DECLARE _inicio DATETIME;
    DECLARE _estado_funcion VARCHAR(20);
    DECLARE _pelicula_activa TINYINT;
    DECLARE _sala_activa TINYINT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_venta FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;
    IF _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;
    SELECT f.id_sala, f.precio_boleto, f.fecha_inicio, f.estado, p.estado, s.estado
    INTO _sala_funcion, _precio, _inicio, _estado_funcion, _pelicula_activa, _sala_activa
    FROM funciones f JOIN peliculas p ON p.id_pelicula = f.id_pelicula
    JOIN salas s ON s.id_sala = f.id_sala WHERE f.id_funcion = _id_funcion FOR UPDATE;
    IF _sala_funcion IS NULL OR _estado_funcion <> 'programada' OR _inicio <= NOW()
        OR _pelicula_activa <> 1 OR _sala_activa <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La función no está disponible para vender boletos.';
    END IF;
    SELECT id_sala, estado INTO _sala_butaca, _butaca_activa
    FROM butacas WHERE id_butaca = _id_butaca FOR UPDATE;
    IF _sala_butaca IS NULL OR _sala_butaca <> _sala_funcion OR _butaca_activa <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seleccionar una butaca activa de la sala de la función.';
    END IF;
    IF EXISTS (SELECT 1 FROM boletos WHERE id_funcion = _id_funcion AND id_butaca = _id_butaca
        AND estado IN ('reservado','vendido')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La butaca ya está reservada o vendida.';
    END IF;
    INSERT INTO boletos (id_venta, id_funcion, id_butaca, id_sala, precio_unitario)
    VALUES (_id_venta, _id_funcion, _id_butaca, _sala_funcion, _precio);
    COMMIT;
    SELECT LAST_INSERT_ID() AS id_boleto;
END $$

CREATE PROCEDURE sp_listarboletosventa(
    IN _id_venta INT
)
BEGIN
    SELECT * FROM vw_lista_boletos WHERE id_venta = _id_venta ORDER BY id_boleto;
END $$

CREATE PROCEDURE sp_buscarboleto(
    IN _id_boleto INT
)
BEGIN
    SELECT * FROM vw_lista_boletos WHERE id_boleto = _id_boleto;
END $$

CREATE PROCEDURE sp_quitarboleto(
    IN _id_venta INT,
    IN _id_boleto INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_venta FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;
    IF _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;
    DELETE FROM boletos WHERE id_boleto = _id_boleto AND id_venta = _id_venta AND estado = 'reservado';
    IF ROW_COUNT() = 0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El boleto no pertenece al carrito.'; END IF;
    COMMIT;
END $$

CREATE PROCEDURE sp_agregarproductoventa(
    IN _id_venta INT,
    IN _id_producto INT,
    IN _cantidad INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE _precio DECIMAL(10,2) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_venta FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;
    IF _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;
    IF _cantidad IS NULL OR _cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad debe ser mayor que cero.';
    END IF;
    SELECT precio INTO _precio FROM productos WHERE id_producto = _id_producto AND estado = 1;
    IF _precio IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no está disponible.'; END IF;
    INSERT INTO detalle_venta_productos (id_venta, id_producto, cantidad, precio_unitario)
    VALUES (_id_venta, _id_producto, _cantidad, _precio)
    ON DUPLICATE KEY UPDATE cantidad = detalle_venta_productos.cantidad + _cantidad;
    COMMIT;
END $$

CREATE PROCEDURE sp_listarproductosventa(
    IN _id_venta INT
)
BEGIN
    SELECT d.id_detalle_producto, d.id_venta, d.id_producto, p.nombre_producto,
        d.cantidad, d.precio_unitario, d.cantidad * d.precio_unitario AS subtotal
    FROM detalle_venta_productos d JOIN productos p ON p.id_producto = d.id_producto
    WHERE d.id_venta = _id_venta ORDER BY d.id_detalle_producto;
END $$

CREATE PROCEDURE sp_actualizarcantidadproducto(
    IN _id_venta INT,
    IN _id_producto INT,
    IN _cantidad INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_venta FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;
    IF _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;
    IF _cantidad IS NULL OR _cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad debe ser mayor que cero.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM detalle_venta_productos WHERE id_venta = _id_venta AND id_producto = _id_producto) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no está en el carrito.';
    END IF;
    UPDATE detalle_venta_productos SET cantidad = _cantidad WHERE id_venta = _id_venta AND id_producto = _id_producto;
    COMMIT;
END $$

CREATE PROCEDURE sp_quitarproductoventa(
    IN _id_venta INT,
    IN _id_producto INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_venta FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;
    IF _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;
    DELETE FROM detalle_venta_productos WHERE id_venta = _id_venta AND id_producto = _id_producto;
    COMMIT;
END $$

CREATE PROCEDURE sp_confirmarventa(
    IN _id_venta INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE _id_usuario_venta INT;
    DECLARE _id_cliente_venta INT;
    DECLARE _id_producto INT;
    DECLARE _cantidad INT;
    DECLARE _stock INT;
    DECLARE _activo TINYINT;
    DECLARE _fin INT DEFAULT 0;
    DECLARE cur_productos CURSOR FOR
        SELECT id_producto, cantidad FROM detalle_venta_productos
        WHERE id_venta = _id_venta ORDER BY id_producto;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET _fin = 1;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado, id_usuario, id_cliente INTO _estado_venta, _id_usuario_venta, _id_cliente_venta
    FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL OR _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya fue confirmada/anulada.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM clientes WHERE id_cliente = _id_cliente_venta AND estado = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta requiere un cliente activo.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol
        WHERE u.id_usuario = _id_usuario_venta AND u.estado = 1 AND r.nombre_rol IN ('admin','taquillero')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El vendedor no está activo o no tiene permiso para vender.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM boletos WHERE id_venta = _id_venta)
        AND NOT EXISTS (SELECT 1 FROM detalle_venta_productos WHERE id_venta = _id_venta) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede confirmar un carrito vacío.';
    END IF;
    IF EXISTS (SELECT 1 FROM boletos b JOIN funciones f ON f.id_funcion = b.id_funcion
        JOIN peliculas p ON p.id_pelicula = f.id_pelicula JOIN salas s ON s.id_sala = f.id_sala
        JOIN butacas bu ON bu.id_butaca = b.id_butaca
        WHERE b.id_venta = _id_venta AND (b.estado <> 'reservado' OR f.estado <> 'programada'
            OR f.fecha_inicio <= NOW() OR p.estado <> 1 OR s.estado <> 1 OR bu.estado <> 1)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hay un boleto que ya no está disponible; corregir el carrito.';
    END IF;
    -- Cada producto se bloquea antes de comprobar/descontar el stock.
    -- Si cualquier producto falla, se revierte TODA la confirmación.
    SET _fin = 0;
    OPEN cur_productos;
    recorrer: LOOP
        FETCH cur_productos INTO _id_producto, _cantidad;
        IF _fin = 1 THEN LEAVE recorrer; END IF;
        SELECT stock, estado INTO _stock, _activo FROM productos WHERE id_producto = _id_producto FOR UPDATE;
        IF _activo <> 1 OR _stock < _cantidad THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hay un producto inactivo o sin stock suficiente.';
        END IF;
        UPDATE productos SET stock = stock - _cantidad WHERE id_producto = _id_producto;
        INSERT INTO movimientos_inventario (id_producto, id_usuario, id_venta, tipo_movimiento, cantidad, observacion)
        VALUES (_id_producto, _id_usuario_venta, _id_venta, 'venta', _cantidad, CONCAT('Venta de confitería #', _id_venta));
    END LOOP;
    CLOSE cur_productos;
    UPDATE boletos SET estado = 'vendido' WHERE id_venta = _id_venta;
    UPDATE ventas SET estado = 'confirmada', fecha_venta = NOW(), fecha_confirmacion = NOW()
    WHERE id_venta = _id_venta;
    COMMIT;
    SELECT * FROM vw_lista_ventas WHERE id_venta = _id_venta;
END $$

CREATE PROCEDURE sp_anularventa(
    IN _id_venta INT,
    IN _id_usuario INT,
    IN _motivo VARCHAR(200)
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE _id_producto INT;
    DECLARE _cantidad INT;
    DECLARE _fin INT DEFAULT 0;
    DECLARE cur_productos CURSOR FOR
        SELECT id_producto, cantidad FROM detalle_venta_productos
        WHERE id_venta = _id_venta ORDER BY id_producto;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET _fin = 1;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_venta FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL OR _estado_venta = 'anulada' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya está anulada.';
    END IF;
    IF _motivo IS NULL OR CHAR_LENGTH(TRIM(_motivo)) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Indicar el motivo de la anulación.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol
        WHERE u.id_usuario = _id_usuario AND u.estado = 1 AND r.nombre_rol IN ('admin','taquillero')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La anulación requiere un taquillero o administrador activo.';
    END IF;
    -- Solo una venta confirmada descontó inventario: restaurarlo una sola vez.
    IF _estado_venta = 'confirmada' THEN
        SET _fin = 0;
        OPEN cur_productos;
        recorrer: LOOP
            FETCH cur_productos INTO _id_producto, _cantidad;
            IF _fin = 1 THEN LEAVE recorrer; END IF;
            UPDATE productos SET stock = stock + _cantidad WHERE id_producto = _id_producto;
            INSERT INTO movimientos_inventario (id_producto, id_usuario, id_venta, tipo_movimiento, cantidad, observacion)
            VALUES (_id_producto, _id_usuario, _id_venta, 'devolucion', _cantidad, _motivo);
        END LOOP;
        CLOSE cur_productos;
    END IF;
    UPDATE boletos SET estado = 'anulado' WHERE id_venta = _id_venta;
    UPDATE ventas SET estado = 'anulada', fecha_anulacion = NOW(), motivo_anulacion = _motivo
    WHERE id_venta = _id_venta;
    COMMIT;
END $$

CREATE PROCEDURE sp_eliminarventaabierta(
    IN _id_venta INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_venta FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe.';
    END IF;
    IF _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta.';
    END IF;
    DELETE FROM boletos WHERE id_venta = _id_venta;
    DELETE FROM detalle_venta_productos WHERE id_venta = _id_venta;
    DELETE FROM ventas WHERE id_venta = _id_venta;
    COMMIT;
END $$

CREATE PROCEDURE sp_verfactura(
    IN _id_venta INT
)
BEGIN
    SELECT * FROM vw_factura_ventas WHERE id_venta = _id_venta ORDER BY tipo_articulo, id_linea;
END $$

CREATE PROCEDURE sp_listarcartelera(

)
BEGIN
    SELECT * FROM vw_cartelera ORDER BY fecha_inicio, nombre_sala;
END $$

CREATE PROCEDURE sp_reporteventas_fechas(
    IN _fecha_desde DATE,
    IN _fecha_hasta DATE
)
BEGIN
    IF _fecha_desde IS NULL OR _fecha_hasta IS NULL OR _fecha_desde > _fecha_hasta THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Indicar un rango de fechas válido.';
    END IF;
    SELECT * FROM vw_lista_ventas WHERE estado = 'confirmada'
        AND fecha_venta >= _fecha_desde AND fecha_venta < DATE_ADD(_fecha_hasta, INTERVAL 1 DAY)
    ORDER BY fecha_venta, id_venta;
END $$

CREATE PROCEDURE sp_reporteventasdia(
    IN _fecha DATE
)
BEGIN
    CALL sp_reporteventas_fechas(_fecha, _fecha);
END $$

CREATE PROCEDURE sp_reporteventassemana(
    IN _fecha DATE
)
BEGIN
    DECLARE _lunes DATE;
    SET _lunes = DATE_SUB(_fecha, INTERVAL WEEKDAY(_fecha) DAY);
    CALL sp_reporteventas_fechas(_lunes, DATE_ADD(_lunes, INTERVAL 6 DAY));
END $$

CREATE PROCEDURE sp_reporteventasmes(
    IN _fecha DATE
)
BEGIN
    DECLARE _primero DATE;
    SET _primero = DATE_SUB(_fecha, INTERVAL (DAYOFMONTH(_fecha) - 1) DAY);
    CALL sp_reporteventas_fechas(_primero, LAST_DAY(_fecha));
END $$

CREATE PROCEDURE sp_indicadoresadmin(

)
BEGIN
    SELECT
        (SELECT COUNT(*) FROM peliculas WHERE estado = 1) AS peliculas_activas,
        (SELECT COUNT(*) FROM clientes WHERE estado = 1) AS clientes_activos,
        (SELECT COUNT(*) FROM usuarios WHERE estado = 1) AS usuarios_activos,
        (SELECT COUNT(*) FROM funciones WHERE estado = 'programada' AND fecha_inicio > NOW()) AS funciones_pendientes,
        (SELECT COUNT(*) FROM vw_stock_critico) AS productos_stock_critico,
        (SELECT COUNT(*) FROM ventas WHERE estado = 'confirmada') AS ventas_confirmadas,
        (SELECT COALESCE(SUM(total_venta), 0) FROM vw_lista_ventas WHERE estado = 'confirmada') AS ventas_totales;
END $$

DELIMITER ;

-- VISTAS PARA TABLEVIEW, CARTELERA, REPORTES Y FACTURAS
-- Alias sin espacios para facilitar ResultSet.get... en los DAO de Java.
CREATE OR REPLACE VIEW vw_lista_clientes AS
SELECT id_cliente, cui, nombre_cliente, apellido_cliente,
    CONCAT(nombre_cliente, ' ', apellido_cliente) AS cliente,
    correo_electronico, telefono, estado, fecha_registro
FROM clientes;

CREATE OR REPLACE VIEW vw_lista_generos AS
SELECT id_genero, nombre_genero, descripcion, estado FROM generos;

CREATE OR REPLACE VIEW vw_lista_peliculas AS
SELECT p.id_pelicula, p.titulo, p.sinopsis, p.director, p.duracion_minutos,
    p.clasificacion, p.idioma, p.fecha_estreno, p.id_genero, g.nombre_genero, p.estado
FROM peliculas p JOIN generos g ON g.id_genero = p.id_genero;

CREATE OR REPLACE VIEW vw_lista_salas AS
SELECT s.id_sala, s.nombre_sala, s.formato, s.estado,
    (SELECT COUNT(*) FROM butacas b WHERE b.id_sala = s.id_sala AND b.estado = 1) AS capacidad
FROM salas s;

CREATE OR REPLACE VIEW vw_lista_funciones AS
SELECT f.id_funcion, f.id_pelicula, p.titulo, p.id_genero, g.nombre_genero,
    p.duracion_minutos, p.clasificacion, p.idioma, f.id_sala, s.nombre_sala, s.formato,
    f.fecha_inicio, f.fecha_fin, f.precio_boleto, f.estado,
    (SELECT COUNT(*) FROM butacas bu WHERE bu.id_sala = f.id_sala AND bu.estado = 1) AS capacidad,
    (SELECT COUNT(*) FROM boletos b WHERE b.id_funcion = f.id_funcion AND b.estado = 'reservado') AS boletos_reservados,
    (SELECT COUNT(*) FROM boletos b WHERE b.id_funcion = f.id_funcion AND b.estado = 'vendido') AS boletos_vendidos,
    (SELECT COUNT(*) FROM butacas bu WHERE bu.id_sala = f.id_sala AND bu.estado = 1
        AND NOT EXISTS (SELECT 1 FROM boletos b WHERE b.id_funcion = f.id_funcion
            AND b.id_butaca = bu.id_butaca AND b.estado IN ('reservado','vendido'))) AS butacas_disponibles
FROM funciones f JOIN peliculas p ON p.id_pelicula = f.id_pelicula
JOIN generos g ON g.id_genero = p.id_genero JOIN salas s ON s.id_sala = f.id_sala;

CREATE OR REPLACE VIEW vw_cartelera AS
SELECT lf.* FROM vw_lista_funciones lf
JOIN peliculas p ON p.id_pelicula = lf.id_pelicula JOIN salas s ON s.id_sala = lf.id_sala
WHERE lf.estado = 'programada' AND lf.fecha_inicio > NOW() AND p.estado = 1 AND s.estado = 1;

CREATE OR REPLACE VIEW vw_lista_productos AS
SELECT p.id_producto, p.nombre_producto, p.descripcion, p.precio, p.stock, p.stock_minimo,
    p.id_categoria_producto, c.nombre_categoria, p.estado
FROM productos p JOIN categorias_producto c ON c.id_categoria_producto = p.id_categoria_producto;

CREATE OR REPLACE VIEW vw_stock_critico AS
SELECT * FROM vw_lista_productos WHERE estado = 1 AND stock <= stock_minimo;

CREATE OR REPLACE VIEW vw_lista_boletos AS
SELECT b.id_boleto, b.id_venta, b.id_funcion, f.id_pelicula, p.titulo,
    b.id_sala, s.nombre_sala, b.id_butaca, bu.fila, bu.numero,
    CONCAT(bu.fila, bu.numero) AS butaca, f.fecha_inicio, b.precio_unitario, b.estado
FROM boletos b JOIN funciones f ON f.id_funcion = b.id_funcion
JOIN peliculas p ON p.id_pelicula = f.id_pelicula JOIN salas s ON s.id_sala = b.id_sala
JOIN butacas bu ON bu.id_butaca = b.id_butaca;

CREATE OR REPLACE VIEW vw_lista_ventas AS
SELECT v.id_venta, v.fecha_venta, v.id_cliente, c.cui,
    CONCAT(c.nombre_cliente, ' ', c.apellido_cliente) AS cliente,
    c.correo_electronico, v.id_usuario,
    CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS taquillero,
    v.estado, v.fecha_confirmacion, v.fecha_anulacion, v.motivo_anulacion,
    COALESCE(bt.cantidad_boletos, 0) AS cantidad_boletos,
    COALESCE(bt.total_boletos, 0) AS total_boletos,
    COALESCE(pr.total_productos, 0) AS total_productos,
    COALESCE(bt.total_boletos, 0) + COALESCE(pr.total_productos, 0) AS total_venta
FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente JOIN usuarios u ON u.id_usuario = v.id_usuario
LEFT JOIN (SELECT id_venta, COUNT(*) AS cantidad_boletos, SUM(precio_unitario) AS total_boletos
    FROM boletos GROUP BY id_venta) bt ON bt.id_venta = v.id_venta
LEFT JOIN (SELECT id_venta, SUM(cantidad * precio_unitario) AS total_productos
    FROM detalle_venta_productos GROUP BY id_venta) pr ON pr.id_venta = v.id_venta;

CREATE OR REPLACE VIEW vw_movimientos_inventario AS
SELECT m.id_movimiento, m.id_producto, p.nombre_producto, m.id_usuario,
    CONCAT(u.nombre_usuario, ' ', u.apellido_usuario) AS usuario,
    m.id_venta, m.tipo_movimiento, m.cantidad, m.fecha_movimiento, m.observacion
FROM movimientos_inventario m JOIN productos p ON p.id_producto = m.id_producto
JOIN usuarios u ON u.id_usuario = m.id_usuario;

-- Una fila por boleto o por producto; el total de la venta se repite en cada fila.
-- Para el pie de factura usar total_venta una sola vez, no SUM(total_venta).
-- Solo ventas confirmadas o anuladas: un carrito abierto aún no es factura.
CREATE OR REPLACE VIEW vw_factura_ventas AS
SELECT v.id_venta, v.fecha_venta AS fecha_emision, v.id_cliente, v.cui, v.cliente,
    v.correo_electronico, v.id_usuario, v.taquillero, v.estado AS estado_venta,
    'boleto' AS tipo_articulo, b.id_boleto AS id_linea,
    CONCAT('Boleto: ', b.titulo, ' / ', b.nombre_sala, ' / ', b.butaca,
        ' / ', DATE_FORMAT(b.fecha_inicio, '%Y-%m-%d %H:%i')) AS descripcion,
    1 AS cantidad, b.precio_unitario, b.precio_unitario AS subtotal, v.total_venta
FROM vw_lista_ventas v JOIN vw_lista_boletos b ON b.id_venta = v.id_venta
WHERE v.estado IN ('confirmada','anulada')
UNION ALL
SELECT v.id_venta, v.fecha_venta, v.id_cliente, v.cui, v.cliente,
    v.correo_electronico, v.id_usuario, v.taquillero, v.estado,
    'producto', d.id_detalle_producto, p.nombre_producto,
    d.cantidad, d.precio_unitario, d.cantidad * d.precio_unitario, v.total_venta
FROM vw_lista_ventas v JOIN detalle_venta_productos d ON d.id_venta = v.id_venta
JOIN productos p ON p.id_producto = d.id_producto WHERE v.estado IN ('confirmada','anulada');

CREATE OR REPLACE VIEW vw_reporte_ventas_diarias AS
SELECT DATE(fecha_venta) AS fecha, COUNT(*) AS cantidad_ventas,
    SUM(cantidad_boletos) AS cantidad_boletos,
    SUM(total_boletos) AS total_boletos, SUM(total_productos) AS total_productos,
    SUM(total_venta) AS total_ventas
FROM vw_lista_ventas WHERE estado = 'confirmada' GROUP BY DATE(fecha_venta);

-- INTEGRACIÓN CON JAVA
-- Modelo: Cliente, Usuario, Rol, Genero, Pelicula, Sala, Butaca, Funcion,
-- Producto, CategoriaProducto, Venta, Boleto, DetalleVentaProducto, MovimientoInventario.
-- DAO: invocar los procedimientos con CallableStatement y parámetros.
-- Para ventas: abrir -> agregar boletos/productos -> confirmar.
-- Cerrar/cancelar un carrito: quitar sus artículos o llamar sp_anularventa.
-- Los boletos reservados ocupan su butaca hasta confirmar, retirar o anular.
-- Cambiar de butaca: quitar el boleto del carrito y agregar el nuevo.
-- Las ventas confirmadas se conservan; para corregirlas, anular y crear otra.
-- La validación completa del correo y de la contraseña corresponde a Java.
-- La cuenta de conexión a MySQL es distinta de los usuarios de estas tablas.
