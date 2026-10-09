-- EJECUTAR SOLO DESPUES DE HACER RESPALDO. NO BORRA VENTAS NI CLIENTES.
USE cinedb_in4cm;
-- Conservamos los roles históricos por la integridad referencial de ventas y movimientos.
-- Las cuentas antiguas quedan deshabilitadas y la aplicación impide volver a asignar esos roles.
UPDATE usuarios u JOIN roles r ON r.id_rol = u.id_rol
SET u.estado = 0 WHERE r.nombre_rol IN ('cliente','bodega');
SELECT r.nombre_rol, COUNT(*) AS cuentas
FROM usuarios u JOIN roles r ON r.id_rol=u.id_rol
GROUP BY r.nombre_rol;
