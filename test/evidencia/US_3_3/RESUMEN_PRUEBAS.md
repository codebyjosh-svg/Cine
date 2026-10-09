# Evidencia de US-3.3 — Joshua

Fecha: 9 de octubre de 2026.

- Compilación del proyecto Apache NetBeans con Java 21 y JavaFX 21.0.11: **correcta**.
- Compilación de las 11 clases de prueba del proyecto: **correcta**.
- Pruebas JDBC/MySQL: **37 comprobaciones correctas**.
- Pruebas JavaFX/FXML: **19 comprobaciones correctas**.
- Total de comprobaciones ejecutadas de US-3.3: **56 correctas**.
- Carga completa de DDL y DML: **correcta** en una BD aislada de prueba.
- Migración ejecutada dos veces: **correcta**; los conteos y existencias originales se conservaron.

## Archivos de evidencia

| Archivo | Contenido |
| --- | --- |
| `compilacion_netbeans_java21.txt` | Resultado real de Ant/NetBeans, BUILD SUCCESSFUL |
| `compilacion_pruebas.txt` | Resultado del compilador de las clases de prueba |
| `prueba_stock_dulceria_bd.txt` | Stock igual, menor, mayor, cero e inactivo; incremento, precio, totales, factura, rollback y concurrencia |
| `prueba_stock_dulceria_fxml.txt` | Operación real de botones, cantidades acumuladas, cliente obligatorio, factura y accesos por rol |
| `conteos_antes.txt`, `conteos_despues.txt` | Comparación de datos antes y después de dos ejecuciones de la migración |
| `01_stock_critico.png` | Listado y filtro de productos críticos |
| `02_venta_dulceria.png` | Tres agregados de 1x en una sola línea, cantidad 3 |
| `03_factura_dulceria.png` | Factura de venta confirmada con vendedor real y totales |

Las pruebas crean y limpian sus propios datos. Las capturas muestran esos datos de prueba.
La prueba concurrente confirmó solo una de dos ventas que competían por la última unidad.
La prueba de rollback comprobó que el fallo de un segundo producto revierte el descuento del primero.
La venta mixta probó un boleto más dulcería y el total general.

La impresión física no se ejecutó: depende de una impresora predeterminada conectada.
La vista y los datos de factura sí se verificaron y se revisaron visualmente.
