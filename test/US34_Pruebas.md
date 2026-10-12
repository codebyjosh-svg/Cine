# Pruebas manuales US-3.4
1. Con MySQL y las vistas/procedimientos del DDL instalados, entrar con administrador: las cuatro tarjetas deben mostrar datos reales.
2. Abrir Reportes: seleccionar Día con ventas, Semana y Mes; cotejar ventas y totales contra `CALL sp_reporteventasdia('2026-10-08')`, semana y mes.
3. Elegir Todas: mostrar todas las ventas CONFIRMADAS (no abiertas/anuladas).
4. Elegir una fecha sin ventas: tabla sin filas, Ventas: 0 y Total: Q 0.
5. Cortar conexión MySQL: mostrar error visible sin inventar ventas ni totales.
6. Seleccionar Volver al dashboard: regresar a vista por rol.
7. Verificar que taquillero no puede abrir Reportes llamando la navegación de administrador.
