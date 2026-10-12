# Cinema — Proyecto de cine

## US-3.2 — Movimientos de inventario

### Descripción

Se implementó el módulo de **Inventario** para el proyecto Cinema, enfocado en la gestión y consulta de movimientos de inventario de productos de dulcería.

### Cambios realizados

- Se implementaron las tareas Java **T3.2.3 a T3.2.12**.
- Se agregaron los modelos necesarios para inventario.
- Se implementaron los DAO y sus respectivas implementaciones.
- Se agregaron los controladores y vistas del módulo Inventario.
- Se integró el módulo con la aplicación.
- Se agregó la consulta de movimientos de inventario.
- Se implementaron las operaciones de entradas y salidas.
- Se realizaron pruebas de la funcionalidad.

### Base de datos

En la documentación original de esta historia se indicaba que las tareas SQL **T3.2.1 y T3.2.2** estaban pendientes y que los scripts existentes se conservaron sin cambios. Este estado debe verificarse con la versión final de la base de datos.

### Pruebas

- Se verificó la compilación del proyecto.
- Se verificó la apertura del módulo Inventario.
- Se probaron las operaciones principales.
- Se revisó la integración con el proyecto.

### Integración

Rama de origen de la historia: `feat/US-3.2_Movimientos_inventario_diego`.

## Notas de ejecución de una versión anterior

En el README original de una de las ramas se indicaba que la configuración de NetBeans se explicaba en `LEEME_JAVAFX_CORREGIDO.txt`, que las bibliotecas estaban en `lib/`, y que se utilizaba `org.cine.system.Lanzador` como clase principal. Verifica estas indicaciones con la versión fusionada antes de seguirlas.

El módulo de programación se documentó bajo **Administrador → Funciones** y `TAREAS_US_2_2_JAVA.md`.
