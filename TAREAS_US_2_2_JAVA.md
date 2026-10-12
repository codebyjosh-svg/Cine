# US-2.2 — Programación de funciones

Las tareas Java T2.2.3–T2.2.12 están implementadas en esta copia de Cine(6).
Responsable: Diego. Prioridad: Must. Estimación total de Java: 12.25 horas.
Cada tarea mantiene como dependencia la tarea anterior del listado original.

| Tarea | Implementación | Archivo principal | Horas |
| --- | --- | --- | ---: |
| T2.2.3 | Modelo con película, sala, horarios, precio y estado | `src/org/cine/model/Funcion.java` | 0.75 |
| T2.2.4 | Contrato de listado, búsqueda, alta, edición, cancelación y finalización | `src/org/cine/dao/FuncionDAO.java` | 0.75 |
| T2.2.5 | Acceso JDBC con `CallableStatement` y cierre automático de recursos | `src/org/cine/dao/impl/FuncionDAOImpl.java` | 2.00 |
| T2.2.6 | Tabla, filtros, formulario, acciones y mensajes | `src/org/cine/view/Programacion.fxml` | 1.75 |
| T2.2.7 | ComboBox con películas y salas activas | `src/org/cine/controller/FuncionController.java` | 1.00 |
| T2.2.8 | Controlador y navegación desde el dashboard del administrador | `src/org/cine/controller/FuncionController.java` | 1.50 |
| T2.2.9 | Alta y edición explícita, con validación de fecha, hora y precio | `src/org/cine/controller/FuncionController.java` | 1.75 |
| T2.2.10 | Cancelar y finalizar con confirmación y conservación del historial | `src/org/cine/controller/FuncionController.java` | 1.00 |
| T2.2.11 | Mensajes de conflicto de horarios, conexión y errores SQL | `src/org/cine/controller/FuncionController.java` | 1.00 |
| T2.2.12 | Pruebas de horarios, estados, acceso e interfaz, con capturas | `test/org/cine/test/` y `test/evidencia/us_2_2/` | 0.75 |

Se agregaron los modelos de apoyo `Pelicula` y `Sala`, el estilo local
`programacion.css` y el botón **Funciones** del dashboard del administrador.

## Uso

1. Abre la carpeta `Cine` en NetBeans y selecciona JDK 21.
2. Configura tu contraseña de MySQL en `src/db.properties`.
3. Ejecuta **Clean and Build** y **Run Project**.
4. Inicia sesión con un administrador y pulsa **Funciones**.
5. Usa **Nueva función** para registrar. Para modificar una función, selecciona
   su fila y pulsa **Editar**; seleccionar una fila por sí solo no carga la edición.

La vista muestra el final previsto según la duración de la película más
20 minutos de limpieza. La base calcula y guarda el final definitivo.
Los conflictos de sala y horario se comunican desde los procedimientos
almacenados. No se puede finalizar antes del final del horario, ni cancelar
una función que tenga boletos reservados o vendidos.

Las dependencias incluidas siguen en **Classpath**, `Modulepath` permanece
vacío y la clase principal sigue siendo `org.cine.system.Lanzador`.
Consulta `LEEME_JAVAFX_CORREGIDO.txt` para los detalles del arranque.

## SQL pendiente

| Tarea | Trabajo pendiente | Horas |
| --- | --- | ---: |
| T2.2.1 | Revisar la tabla funciones y sus relaciones con películas y salas | 0.75 |
| T2.2.2 | Revisar los procedimientos de insertar, actualizar, cancelar y finalizar | 1.00 |

Estas dos tareas quedan pendientes hasta recibir los SQL del usuario. Los
archivos `sql/cinedb_script_ddl.sql` y `sql/cinedb_script_dml.sql` originales se
conservaron sin modificaciones. Las pruebas Java usaron sus procedimientos
existentes en una base temporal.

El DAO espera los procedimientos existentes `sp_listarfunciones`,
`sp_buscarfuncion`, `sp_insertarfuncion`, `sp_actualizarfuncion`,
`sp_cancelarfuncion`, `sp_finalizarfuncion`, `sp_listarpeliculas` y
`sp_listarsalas`. Si falta alguno en la base instalada, la pantalla informa
que la integración SQL está pendiente.

## Verificación

Pasaron 50 comprobaciones específicas del módulo: 27 de DAO y horarios,
19 de interfaz y 4 de errores SQL. También pasaron las pruebas anteriores de
login, seguridad y navegación, y el arranque real mediante Ant con Classpath.

Los resultados y las tres capturas están en `test/evidencia/us_2_2/`.
Las instrucciones para repetir las pruebas están en `test/PRUEBAS_US_2_2.md`.
