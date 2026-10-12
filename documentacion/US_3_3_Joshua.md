# US-3.3 — Stock crítico y venta de dulcería

**Desarrollador:** Joshua. **Prioridad:** Must para todas las tareas.
**Tiempo estimado original:** 14 horas. Cada tarea depende de la anterior,
excepto T3.3.1. El tiempo es una estimación del backlog, no tiempo medido.

| Tarea | Trabajo implementado | Archivos principales | Horas |
| --- | --- | --- | ---: |
| T3.3.1 | Revisar vista y procedimiento; conservar productos activos y regla `stock <= stock_minimo`; entregar actualización repetible | `sql/cinedb_script_ddl.sql`, `sql/actualizacion_US_3_3_stock_dulceria.sql` | 0.75 |
| T3.3.2 | DTO de stock con categoría, mínimo y nivel de alerta | `src/org/cine/model/StockCritico.java`, `Producto.java` | 0.75 |
| T3.3.3 | Contrato de consulta de productos críticos | `src/org/cine/dao/StockCriticoDAO.java` | 0.75 |
| T3.3.4 | Consulta JDBC mediante `sp_listarstockcritico()` | `src/org/cine/dao/impl/StockCriticoDAOImpl.java` | 1.25 |
| T3.3.5 | Listado, alertas, filtros, conteos y tabla con scroll interno | `src/org/cine/view/StockCritico.fxml`, `view/style/operaciones.css` | 1.50 |
| T3.3.6 | Controlador con carga en segundo plano, actualización, filtros y errores visibles | `src/org/cine/controller/StockCriticoController.java` | 1.25 |
| T3.3.7 | Catálogo de dulcería en venta, cliente obligatorio, carrito, retiro y confirmación | `src/org/cine/controller/VentaController.java`, `view/Venta.fxml`, `dao/VentaDAO.java`, `dao/impl/VentaDAOImpl.java` | 2.00 |
| T3.3.8 | Incrementar cantidad por producto sin duplicar filas; conservar el precio inicial | `VentaController.java`, `sp_agregarproductoventa`, clave `uq_detalle_venta_producto` | 1.00 |
| T3.3.9 | Validar stock acumulado al agregar y editar; prevalidar y volver a bloquear al confirmar | `VentaController.java`, `VentaDAOImpl.java`, procedimientos SQL | 1.00 |
| T3.3.10 | Subtotal de dulcería, subtotal de boletos y total general; factura del registro confirmado | `VentaController.java`, `view/Factura.fxml`, `controller/FacturaController.java`, DTOs de venta/factura | 1.00 |
| T3.3.11 | Casos igual, menor, mayor, cero, inactivo, cantidad insuficiente, rollback y concurrencia | `test/org/cine/test/PruebaStockDulceriaBD.java`, `test/evidencia/US_3_3/` | 1.25 |
| T3.3.12 | Tres agregados de 1x conservan una fila con cantidad 3, también al operar los botones reales | `PruebaStockDulceriaBD.java`, `PruebaStockDulceriaFXML.java`, evidencias | 1.50 |

## Decisiones de integración

El archivo recibido incluía login, usuarios, películas, géneros y scripts SQL
de ventas, pero no una implementación Java de ventas ni factura. Se agregaron
`VentaDAO`, `VentaDAOImpl`, modelos de línea/encabezado/factura y las vistas
necesarias para realizar la venta de dulcería de principio a fin.

Se puede retomar una venta SQL abierta con boletos. El carrito muestra sus
productos y conserva el subtotal de boletos del encabezado; la factura incluye
ambos tipos de artículo. La venta visual de boletos no forma parte de esta US.

Los dashboards de taquillero, bodega y cliente referenciaban un
`DashboardController` y un CSS ausentes en el ZIP. Se agregaron para que la
navegación por rol pueda cargar. Admin y taquillero pueden vender; bodega puede
consultar stock; cliente no puede abrir los módulos de personal.

El procedimiento de login omitía `correo_electronico`, `estado` y
`fecha_registro`, aunque el DAO existente lee esos campos. La actualización
SQL agrega los campos y permite que Java distinga las cuentas inactivas.

Las nuevas pantallas cambian de escena en la misma ventana y usan azul oscuro,
azul, blanco y rojo del dashboard recibido. El scroll se limita a las tablas.

## Reglas verificadas

- Crítico: producto activo con `stock <= stock_minimo`.
- Agotar existencias: `stock == 0` es una alerta incluso con mínimo cero.
- Agregar es un incremento; Guardar cantidad es un reemplazo del valor.
- Una línea por `(id_venta, id_producto)`, con cantidad entera positiva.
- El precio unitario se conserva desde el primer agregado al carrito.
- Subtotal de línea = cantidad × precio unitario, usando `BigDecimal`.
- Total general = total de boletos + subtotal de dulcería.
- El carrito no reserva stock. Dos vendedores pueden tener el mismo producto;
  la confirmación final decide según las existencias bloqueadas en la BD.
- La confirmación usa `sp_confirmarventa`, que bloquea los productos en orden
  de ID, descuenta existencias y registra los movimientos en una transacción.
- Si cualquier línea falla, se revierten todos los descuentos y movimientos.
- La venta confirmada no puede volver a confirmarse ni editarse.
- La actualización SQL cambia vistas/procedimientos; no borra datos existentes.

## Ejecutar las pruebas en NetBeans

1. Configure JDK 21, la conexión MySQL y ejecute la actualización SQL.
2. Compile el proyecto.
3. Ejecute `PruebaStockDulceriaBD.java` con Run File desde Test Packages.
4. Ejecute `PruebaStockDulceriaFXML.java` con Run File. Usa las cuentas de
   demostración `admin.cine`, `taquilla.cine`, `bodega.cine` y `cliente.cine`.
5. Consulte la salida y `test/evidencia/US_3_3/`.

Las pruebas usan datos con un nombre propio y los limpian en `finally`.
La prueba de BD incluye una función, sala y butaca propias para la venta mixta.
La prueba de interfaz utiliza las vistas y botones reales de la aplicación.

La impresión física depende de una impresora predeterminada disponible;
la verificación automatizada cubre los datos y la vista de factura.
