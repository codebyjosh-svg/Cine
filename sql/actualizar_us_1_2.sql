-- Actualiza US-1.2 y mantiene el login de US-1.1.
-- Usar cuando las tablas y los datos del cine ya existen.
USE cinedb_in4cm;
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_insertarusuario $$
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

DROP PROCEDURE IF EXISTS sp_actualizarusuario $$
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

DROP PROCEDURE IF EXISTS sp_eliminarusuario $$
CREATE PROCEDURE sp_eliminarusuario(IN _id_usuario INT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = _id_usuario) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El usuario ya no existe.';
    END IF;
    DELETE FROM usuarios WHERE id_usuario = _id_usuario;
END $$

DROP PROCEDURE IF EXISTS sp_cambiarestadousuario $$
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

DROP PROCEDURE IF EXISTS sp_cambiarcontrasena $$
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

DROP PROCEDURE IF EXISTS sp_buscarusuario_login $$
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

DELIMITER ;
