# US-3.4 — Indicadores administrativos

Fuente: `CALL sp_indicadoresadmin()` del DDL existente. En el dashboard se muestran:

- Usuarios activos (`usuarios.estado = 1`).
- Películas activas (`peliculas.estado = 1`).
- Clientes activos (`clientes.estado = 1`).
- Ventas confirmadas (`ventas.estado = 'confirmada'`).

El procedimiento también devuelve funciones pendientes, stock crítico y la suma monetaria de ventas confirmadas. Se conserva su API para futuras tarjetas.

## Reportes

- **Día:** `sp_reporteventasdia(fecha)`.
- **Semana:** `sp_reporteventassemana(fecha)`; semana de lunes a domingo.
- **Mes:** `sp_reporteventasmes(fecha)`; mes calendario.
- **Todas:** consulta sobre `vw_lista_ventas`, solo confirmadas.
- Total: suma decimal de `total_venta` de filas visibles. No se cuentan ventas abiertas o anuladas.
- Un período sin ventas muestra tabla vacía, contador 0 y total Q 0.

Requiere instalar el DDL del proyecto, que ya incluye vistas y procedimientos para los reportes.
