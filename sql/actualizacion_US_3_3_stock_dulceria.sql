-- US-3.3 / Joshua / T3.3.1 a T3.3.12
-- Ejecutar UNA VEZ sobre la BD existente (es seguro volver a ejecutarlo).
-- No elimina tablas, productos, ventas ni movimientos.
SET NAMES utf8mb4;
USE cinedb_in4cm;

CREATE OR REPLACE VIEW vw_stock_critico AS
SELECT id_producto, nombre_producto, descripcion, precio, stock, stock_minimo,
    id_categoria_producto, nombre_categoria, estado
FROM vw_lista_productos
WHERE estado = 1 AND stock <= stock_minimo;

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_listarstockcritico $$
CREATE PROCEDURE sp_listarstockcritico()
BEGIN
    SELECT * FROM vw_stock_critico ORDER BY stock, nombre_producto;
END $$

DROP PROCEDURE IF EXISTS sp_listarproductosdisponibles $$
CREATE PROCEDURE sp_listarproductosdisponibles()
BEGIN
    SELECT p.id_producto, p.nombre_producto, p.precio, p.stock, p.stock_minimo,
        p.estado, c.nombre_categoria
    FROM productos p JOIN categorias_producto c
        ON c.id_categoria_producto = p.id_categoria_producto
    WHERE p.estado = 1 AND c.estado = 1
    ORDER BY c.nombre_categoria, p.nombre_producto;
END $$

DROP PROCEDURE IF EXISTS sp_agregarproductoventa $$
CREATE PROCEDURE sp_agregarproductoventa(
    IN _id_venta INT,
    IN _id_producto INT,
    IN _cantidad INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE _precio DECIMAL(10,2) DEFAULT NULL;
    DECLARE _stock INT DEFAULT NULL;
    DECLARE _acumulada BIGINT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_venta FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL OR _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta existente.';
    END IF;
    IF _cantidad IS NULL OR _cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad debe ser mayor que cero.';
    END IF;
    SELECT precio, stock INTO _precio, _stock FROM productos
    WHERE id_producto = _id_producto AND estado = 1 FOR UPDATE;
    IF _precio IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no está disponible.';
    END IF;
    SET _acumulada = COALESCE((SELECT cantidad FROM detalle_venta_productos
        WHERE id_venta = _id_venta AND id_producto = _id_producto), 0) + _cantidad;
    IF _acumulada > _stock THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuficiente para la cantidad acumulada del producto.';
    END IF;
    -- uq_detalle_venta_producto garantiza una sola línea por producto y venta.
    -- Se conserva el precio capturado cuando se agregó por primera vez.
    INSERT INTO detalle_venta_productos (id_venta, id_producto, cantidad, precio_unitario)
    VALUES (_id_venta, _id_producto, _cantidad, _precio)
    ON DUPLICATE KEY UPDATE cantidad = detalle_venta_productos.cantidad + _cantidad;
    COMMIT;
END $$

DROP PROCEDURE IF EXISTS sp_actualizarcantidadproducto $$
CREATE PROCEDURE sp_actualizarcantidadproducto(
    IN _id_venta INT,
    IN _id_producto INT,
    IN _cantidad INT
)
BEGIN
    DECLARE _estado_venta VARCHAR(20) DEFAULT NULL;
    DECLARE _stock INT DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO _estado_venta FROM ventas WHERE id_venta = _id_venta FOR UPDATE;
    IF _estado_venta IS NULL OR _estado_venta <> 'abierta' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede modificar una venta abierta existente.';
    END IF;
    IF _cantidad IS NULL OR _cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad debe ser mayor que cero.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM detalle_venta_productos
        WHERE id_venta = _id_venta AND id_producto = _id_producto) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no está en el carrito.';
    END IF;
    SELECT stock INTO _stock FROM productos
    WHERE id_producto = _id_producto AND estado = 1 FOR UPDATE;
    IF _stock IS NULL OR _cantidad > _stock THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Producto inactivo o stock insuficiente para la nueva cantidad.';
    END IF;
    UPDATE detalle_venta_productos SET cantidad = _cantidad
    WHERE id_venta = _id_venta AND id_producto = _id_producto;
    COMMIT;
END $$

DROP PROCEDURE IF EXISTS sp_validarstockventa $$
CREATE PROCEDURE sp_validarstockventa(IN _id_venta INT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ventas WHERE id_venta = _id_venta AND estado = 'abierta') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta no existe o ya fue confirmada/anulada.';
    END IF;
    -- Es una prevalidación informativa. sp_confirmarventa realiza el bloqueo definitivo.
    SELECT d.id_producto, p.nombre_producto, d.cantidad, p.stock, p.estado
    FROM detalle_venta_productos d JOIN productos p ON p.id_producto = d.id_producto
    WHERE d.id_venta = _id_venta AND (p.estado <> 1 OR d.cantidad > p.stock)
    ORDER BY d.id_producto;
END $$

-- Compatibilidad con el DAO de login del proyecto recibido.
DROP PROCEDURE IF EXISTS sp_buscarusuario_login $$
CREATE PROCEDURE sp_buscarusuario_login(IN _username VARCHAR(50))
BEGIN
    -- Java verifica la contraseña y después rechaza las cuentas inactivas.
    -- Se devuelven todos los campos esperados por UsuarioDAOImpl.convertir.
    SELECT u.id_usuario, u.nombre_usuario, u.apellido_usuario, u.username,
        u.correo_electronico, u.contrasena_hash, u.id_rol, r.nombre_rol,
        u.id_cliente, u.estado, u.fecha_registro
    FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol
    WHERE u.username = TRIM(_username);
END $$

DELIMITER ;

-- Comprobación de lectura: CALL sp_listarstockcritico();
