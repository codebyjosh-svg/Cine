-- ============================================================
-- 01_DDL_Cine.sql
-- Tablas, relaciones y vistas del cine. Ejecutar antes de 02 y 03.
-- Sistema de Gestion Integral para Cine
-- Base: cinedb_in4cm
-- 14 tablas - cobertura Sprints 1, 2 y 3
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS cinedb_in4cm;

CREATE DATABASE cinedb_in4cm
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cinedb_in4cm;

-- ============================================================
-- SPRINT 1
-- ============================================================

CREATE TABLE roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(20) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NOT NULL,
    CONSTRAINT chk_rol_nombre
        CHECK (nombre_rol IN ('admin', 'taquillero', 'bodega', 'cliente'))
) ENGINE=InnoDB;

CREATE TABLE clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    cui CHAR(13) NULL UNIQUE,
    nit VARCHAR(20) NOT NULL DEFAULT 'CF',
    nombre_cliente VARCHAR(100) NOT NULL,
    apellido_cliente VARCHAR(100) NOT NULL,
    correo_electronico VARCHAR(120) NOT NULL UNIQUE,
    telefono VARCHAR(20) NULL,
    estado TINYINT NOT NULL DEFAULT 1,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cliente_estado CHECK (estado IN (0,1)),
    CONSTRAINT chk_clientes_correo_formato CHECK ((COALESCE(CHAR_LENGTH(correo_electronico) BETWEEN 3 AND 120 AND CHAR_LENGTH(SUBSTRING_INDEX(correo_electronico, '@', 1)) <= 64 AND REGEXP_LIKE(correo_electronico, '^[A-Za-z0-9_%+-]+([.][A-Za-z0-9_%+-]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?([.][A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*[.][A-Za-z]{2,63}$', 'c'), 0) = 1))
) ENGINE=InnoDB;

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(100) NOT NULL,
    apellido_usuario VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    correo_electronico VARCHAR(120) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    id_cliente INT NULL UNIQUE,
    estado TINYINT NOT NULL DEFAULT 1,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_usuario_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_usuario_estado CHECK (estado IN (0,1)),
    CONSTRAINT chk_usuarios_correo_formato CHECK ((COALESCE(CHAR_LENGTH(correo_electronico) BETWEEN 3 AND 120 AND CHAR_LENGTH(SUBSTRING_INDEX(correo_electronico, '@', 1)) <= 64 AND REGEXP_LIKE(correo_electronico, '^[A-Za-z0-9_%+-]+([.][A-Za-z0-9_%+-]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?([.][A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*[.][A-Za-z]{2,63}$', 'c'), 0) = 1))
) ENGINE=InnoDB;

CREATE TABLE generos (
    id_genero INT AUTO_INCREMENT PRIMARY KEY,
    nombre_genero VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    estado TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT chk_genero_estado CHECK (estado IN (0,1))
) ENGINE=InnoDB;

CREATE TABLE peliculas (
    id_pelicula INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    sinopsis TEXT,
    director VARCHAR(150),
    duracion_minutos INT NOT NULL,
    clasificacion VARCHAR(20) NOT NULL,
    idioma VARCHAR(50) NOT NULL DEFAULT 'Español',
    fecha_estreno DATE,
    imagen LONGBLOB NULL,
    id_genero INT NOT NULL,
    estado TINYINT NOT NULL DEFAULT 1,

    CONSTRAINT fk_pelicula_genero
        FOREIGN KEY (id_genero)
        REFERENCES generos(id_genero)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_pelicula_duracion
        CHECK (duracion_minutos BETWEEN 1 AND 600),

    CONSTRAINT chk_pelicula_estado
        CHECK (estado IN (0,1))
) ENGINE=InnoDB;

CREATE INDEX idx_peliculas_titulo ON peliculas(titulo);
CREATE INDEX idx_peliculas_estado ON peliculas(estado);

-- ============================================================
-- SPRINT 2
-- ============================================================

CREATE TABLE salas (
    id_sala INT AUTO_INCREMENT PRIMARY KEY,
    nombre_sala VARCHAR(100) NOT NULL UNIQUE,
    formato VARCHAR(20) NOT NULL DEFAULT '2D',
    estado TINYINT NOT NULL DEFAULT 1,

    CONSTRAINT chk_sala_formato
        CHECK (formato IN ('2D','3D')),

    CONSTRAINT chk_sala_estado
        CHECK (estado IN (0,1))
) ENGINE=InnoDB;

CREATE TABLE butacas (
    id_butaca INT AUTO_INCREMENT PRIMARY KEY,
    id_sala INT NOT NULL,
    fila VARCHAR(3) NOT NULL,
    numero INT NOT NULL,
    estado TINYINT NOT NULL DEFAULT 1,

    CONSTRAINT uq_butaca_ubicacion
        UNIQUE (id_sala, fila, numero),

    CONSTRAINT fk_butaca_sala
        FOREIGN KEY (id_sala)
        REFERENCES salas(id_sala)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_butaca_numero
        CHECK (numero > 0),

    CONSTRAINT chk_butaca_estado
        CHECK (estado IN (0,1))
) ENGINE=InnoDB;

CREATE INDEX idx_butacas_sala ON butacas(id_sala);
CREATE UNIQUE INDEX uq_butaca_id_sala ON butacas(id_butaca, id_sala);

CREATE TABLE funciones (
    id_funcion INT AUTO_INCREMENT PRIMARY KEY,
    id_pelicula INT NOT NULL,
    id_sala INT NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    precio_boleto DECIMAL(10,2) NOT NULL,
    estado ENUM('programada','finalizada','cancelada')
        NOT NULL DEFAULT 'programada',

    CONSTRAINT fk_funcion_pelicula
        FOREIGN KEY (id_pelicula)
        REFERENCES peliculas(id_pelicula)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_funcion_sala
        FOREIGN KEY (id_sala)
        REFERENCES salas(id_sala)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_funcion_fechas
        CHECK (fecha_fin > fecha_inicio),

    CONSTRAINT chk_funcion_precio
        CHECK (precio_boleto > 0)
) ENGINE=InnoDB;

CREATE INDEX idx_funciones_sala_horario
    ON funciones(id_sala, estado, fecha_inicio, fecha_fin);

CREATE INDEX idx_funciones_pelicula
    ON funciones(id_pelicula, estado);

CREATE UNIQUE INDEX uq_funcion_id_sala
    ON funciones(id_funcion, id_sala);

CREATE TABLE ventas (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    fecha_venta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_cliente INT NOT NULL,
    id_usuario INT NOT NULL,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    metodo_pago VARCHAR(30) NOT NULL DEFAULT 'EFECTIVO',
    estado ENUM('abierta','confirmada','anulada')
        NOT NULL DEFAULT 'abierta',
    fecha_confirmacion DATETIME NULL,
    fecha_anulacion DATETIME NULL,
    motivo_anulacion VARCHAR(200) NULL,

    CONSTRAINT fk_venta_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_venta_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_venta_total
        CHECK (total >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_ventas_fecha ON ventas(fecha_venta);
CREATE INDEX idx_ventas_estado ON ventas(estado);

CREATE TABLE boletos (
    id_boleto INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_funcion INT NOT NULL,
    id_butaca INT NOT NULL,
    id_sala INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    estado ENUM('reservado','vendido','anulado')
        NOT NULL DEFAULT 'reservado',

    CONSTRAINT fk_boleto_venta
        FOREIGN KEY (id_venta)
        REFERENCES ventas(id_venta)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_boleto_funcion_sala
        FOREIGN KEY (id_funcion, id_sala)
        REFERENCES funciones(id_funcion, id_sala)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_boleto_butaca_sala
        FOREIGN KEY (id_butaca, id_sala)
        REFERENCES butacas(id_butaca, id_sala)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_boleto_precio
        CHECK (precio_unitario > 0)
) ENGINE=InnoDB;

CREATE INDEX idx_boletos_venta ON boletos(id_venta);
CREATE INDEX idx_boletos_funcion ON boletos(id_funcion, estado);

-- Impide dos boletos activos para la misma función y butaca.
-- Los boletos anulados no participan en esta restricción.
ALTER TABLE boletos
    ADD COLUMN id_ocupacion INT
    GENERATED ALWAYS AS (
        CASE
            WHEN estado IN ('reservado','vendido')
            THEN id_funcion
            ELSE NULL
        END
    ) STORED;

ALTER TABLE boletos
    ADD UNIQUE KEY uq_boleto_funcion_butaca_activo
        (id_ocupacion, id_butaca);

-- ============================================================
-- SPRINT 3
-- ============================================================

CREATE TABLE categorias_producto (
    id_categoria_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    estado TINYINT NOT NULL DEFAULT 1,

    CONSTRAINT chk_categoria_producto_estado
        CHECK (estado IN (0,1))
) ENGINE=InnoDB;

CREATE TABLE productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    id_categoria_producto INT NOT NULL,
    nombre_producto VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 5,
    estado TINYINT NOT NULL DEFAULT 1,

    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (id_categoria_producto)
        REFERENCES categorias_producto(id_categoria_producto)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_producto_precio CHECK (precio >= 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0),
    CONSTRAINT chk_producto_stock_minimo CHECK (stock_minimo >= 0),
    CONSTRAINT chk_producto_estado CHECK (estado IN (0,1))
) ENGINE=InnoDB;

CREATE INDEX idx_productos_stock
    ON productos(stock, stock_minimo);

CREATE TABLE detalle_venta_productos (
    id_detalle_producto INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_detalle_producto_venta
        FOREIGN KEY (id_venta)
        REFERENCES ventas(id_venta)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_detalle_producto_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT uq_venta_producto UNIQUE (id_venta, id_producto),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_detalle_subtotal CHECK (subtotal >= 0 AND subtotal = cantidad * precio_unitario)
) ENGINE=InnoDB;

CREATE INDEX idx_detalle_venta
    ON detalle_venta_productos(id_venta);

CREATE TABLE movimientos_inventario (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    id_usuario INT NOT NULL,
    id_venta INT NULL,
    tipo_movimiento ENUM('ENTRADA','SALIDA','AJUSTE') NOT NULL,
    cantidad INT NOT NULL,
    fecha_movimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion VARCHAR(255),

    CONSTRAINT fk_movimiento_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_movimiento_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_movimiento_venta
        FOREIGN KEY (id_venta)
        REFERENCES ventas(id_venta)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT chk_movimiento_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

CREATE INDEX idx_movimientos_producto
    ON movimientos_inventario(id_producto, fecha_movimiento);

-- ============================================================
-- VISTAS BASE
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
SELECT
    id_genero,
    nombre_genero,
    descripcion,
    estado
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
INNER JOIN generos g
    ON g.id_genero = p.id_genero;

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
INNER JOIN peliculas p
    ON p.id_pelicula = f.id_pelicula
INNER JOIN generos g
    ON g.id_genero = p.id_genero
INNER JOIN salas s
    ON s.id_sala = f.id_sala;

CREATE OR REPLACE VIEW vw_cartelera AS
SELECT
    lf.*
FROM vw_lista_funciones lf
WHERE lf.estado = 'programada'
  AND lf.fecha_inicio > NOW()
  AND EXISTS (
      SELECT 1
      FROM peliculas p
      WHERE p.id_pelicula = lf.id_pelicula
        AND p.estado = 1
  )
  AND EXISTS (
      SELECT 1
      FROM salas s
      WHERE s.id_sala = lf.id_sala
        AND s.estado = 1
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
INNER JOIN funciones f
    ON f.id_funcion = b.id_funcion
INNER JOIN peliculas p
    ON p.id_pelicula = f.id_pelicula
INNER JOIN salas s
    ON s.id_sala = b.id_sala
INNER JOIN butacas bu
    ON bu.id_butaca = b.id_butaca;

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
        SELECT COALESCE(SUM(b.precio_unitario), 0)
        FROM boletos b
        WHERE b.id_venta = v.id_venta
          AND b.estado IN ('reservado','vendido')
    ) AS total_boletos,
    (
        SELECT COALESCE(SUM(d.subtotal), 0)
        FROM detalle_venta_productos d
        WHERE d.id_venta = v.id_venta
    ) AS total_productos
FROM ventas v
INNER JOIN clientes c
    ON c.id_cliente = v.id_cliente
INNER JOIN usuarios u
    ON u.id_usuario = v.id_usuario;

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
INNER JOIN productos p
    ON p.id_producto = m.id_producto
INNER JOIN usuarios u
    ON u.id_usuario = m.id_usuario;

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
INNER JOIN clientes c
    ON c.id_cliente = v.id_cliente
INNER JOIN usuarios u
    ON u.id_usuario = v.id_usuario;

CREATE OR REPLACE VIEW vw_reporte_ventas_diarias AS
SELECT
    DATE(fecha_venta) AS fecha,
    COUNT(*) AS cantidad_ventas,
    COALESCE(SUM(total), 0) AS total_ventas
FROM ventas
WHERE estado = 'confirmada'
GROUP BY DATE(fecha_venta);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- FIN DDL
-- ============================================================
