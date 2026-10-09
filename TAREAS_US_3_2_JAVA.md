# US-3.2 — Movimientos de inventario

Se implementaron las tareas Java T3.2.3–T3.2.12 sobre la copia adjunta de
Cine(7). Responsable: Diego. Estimación total de Java: 12.25 horas.
Cada tarea mantiene como dependencia la tarea anterior del listado original.

| Tarea | Implementación | Archivo principal | Horas | Prioridad |
| --- | --- | --- | ---: | --- |
| T3.2.3 | Modelo con producto, operador, tipo, cantidad, fecha y observación | `src/org/cine/model/MovimientoInventario.java` | 0.75 | Must |
| T3.2.4 | Contrato de entradas, salidas, productos activos e historial | `src/org/cine/dao/MovimientoInventarioDAO.java` | 0.75 | Must |
| T3.2.5 | DAO con CallableStatement y cierre automático de recursos | `src/org/cine/dao/impl/MovimientoInventarioDAOImpl.java` | 2.00 | Must |
| T3.2.6 | Pantalla de inventario integrada al dashboard | `src/org/cine/view/Inventario.fxml` | 1.75 | Must |
| T3.2.7 | Formulario, stock actual, búsqueda, filtros y tabla de historial | `src/org/cine/view/Inventario.fxml` y `style/inventario.css` | 1.25 | Must |
| T3.2.8 | Controlador y navegación para administrador y bodega | `src/org/cine/controller/InventarioController.java` | 1.50 | Must |
| T3.2.9 | Registro de entrada y recarga automática del stock y el historial | `src/org/cine/controller/InventarioController.java` | 1.00 | Must |
| T3.2.10 | Salida con validación de stock actual y manejo del rechazo SQL | `src/org/cine/controller/InventarioController.java` | 1.25 | Must |
| T3.2.11 | Usuario de sesión, fecha del servidor, tipo y observación visible | `src/org/cine/controller/InventarioController.java` | 0.75 | Should |
| T3.2.12 | Pruebas de entrada, salida, stock insuficiente e historial y capturas | `test/org/cine/test/` y `test/evidencia/us_3_2/` | 1.25 | Must |

Se agregó también el modelo de apoyo `Producto` con los datos necesarios
para seleccionar un producto y consultar su stock.

## Uso del módulo

1. Inicia sesión con un administrador o un usuario de bodega.
2. Pulsa **Inventario** en su dashboard.
3. Selecciona un producto activo, elige **Entrada** o **Salida**, escribe una
   cantidad entera positiva y una observación de hasta 200 caracteres.
4. Pulsa **Registrar**. Al confirmar el procedimiento se recargan el stock
   y el historial y se limpian la cantidad y la observación.
5. Usa **Actualizar** para consultar cambios de otros operadores y **Volver**
   para regresar al dashboard conservando la sesión.

Antes de registrar una salida, Java vuelve a consultar el producto para
validar el stock actual. El procedimiento existente confirma el movimiento
y el cambio de stock en su propia transacción; el DAO no agrega una
transacción externa.

El historial es de consulta: conserva los movimientos y muestra las
entradas, salidas, ventas y devoluciones existentes. Los movimientos manuales
solo permiten Entrada o Salida. No se agregaron edición ni borrado del historial.

El usuario se toma de la sesión; la fecha se obtiene de la base de datos.
Taquilleros y clientes no tienen acceso al registro manual de inventario.

## Arranque en NetBeans

Selecciona **JDK 21** en Properties → Libraries → Java Platform. JavaFX
21.0.11 para Windows x64 y Connector/J 8.0.33 están incluidos en `lib/`
con rutas relativas en **Classpath**. Modulepath y Run → VM Options quedan
vacíos. La clase principal es `org.cine.system.Lanzador`.

Configura tu contraseña de MySQL en `src/db.properties`, ejecuta
**Clean and Build** y **Run Project**. La configuración de conexión original
se conservó; su contraseña venía vacía. Si clonas el repositorio, copia
`src/db.properties.example` a `src/db.properties` y configura tu conexión.
El archivo local `db.properties` está excluido por el gitignore del proyecto.

La cuenta de aplicación de ejemplo es `admin.cine / AdminCine2026!`, o
`bodega.cine / BodegaCine2026!`. Estas claves son del login del cine.

## SQL pendiente

T3.2.1 y T3.2.2 permanecen pendientes hasta recibir los SQL para esas tareas.
Los archivos SQL originales del ZIP se conservaron sin modificaciones.

Java utiliza los procedimientos existentes:
`sp_registrarmovimientoinventario`, `sp_listarmovimientosinventario`,
`sp_listarproductos` y `sp_buscarproducto`. Si falta alguno en la base
instalada, la pantalla informa el problema de integración SQL.

## Verificación

Pasaron **57 comprobaciones** del módulo: 28 de integración JDBC, 25 de
interfaz y 4 de errores SQL. Se probaron salidas simultáneas, stock insuficiente,
stock cambiado por otro operador, productos inactivos, permisos y validaciones.
También pasaron las pruebas anteriores de login, seguridad y navegación,
y el arranque real mediante Ant.

Las instrucciones para repetir las pruebas están en `test/PRUEBAS_US_3_2.md`.
Los resultados y las cuatro capturas están en `test/evidencia/us_3_2/`.
