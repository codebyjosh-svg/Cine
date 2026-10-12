# Pruebas Java de programación de funciones — T2.2.12

Verificado el 8 de octubre de 2026 con JDK 21, JavaFX 21.0.11,
MySQL 8.0.46, MySQL Connector/J 8.0.33 y Ant 1.10.15.
El entorno de pruebas Linux utilizó JavaFX para Linux y Monocle para abrir
las ventanas sin monitor. El proyecto entregado conserva JavaFX para Windows
x64 en Classpath, con Modulepath y opciones de VM vacíos.

## Preparación

Usa una copia desechable de la base, con la estructura y los datos de ejemplo
de los SQL actuales. Configura `src/db.properties` para esa copia antes de
compilar. Las pruebas crean y retiran películas, salas, butacas, funciones,
ventas y boletos de prueba; no deben ejecutarse sobre una base de uso real.

Las pruebas de interfaz usan las cuentas de ejemplo de administrador y
taquillero del DML actual. El indicador `-Dcine.pruebas.bd=true` confirma
explícitamente que se está trabajando sobre la copia de pruebas.

Desde la carpeta del proyecto, en una consola de Windows con JDK 21 y Ant:

```bat
ant clean jar compile-test
java -Dcine.pruebas.bd=true -cp "build\classes;build\test\classes;lib\*;lib\javafx-21.0.11\*" org.cine.test.PruebaFunciones
java -Dcine.pruebas.bd=true -cp "build\classes;build\test\classes;lib\*;lib\javafx-21.0.11\*" org.cine.test.LanzadorPruebaProgramacion
```

También puedes compilar con NetBeans y ejecutar esas clases desde el editor,
añadiendo el indicador de pruebas a las opciones de VM de la ejecución de prueba.

## Cobertura y resultados

| Prueba | Comprobaciones | Resultado |
| --- | ---: | --- |
| `PruebaFunciones` | 27 | Correctas |
| `PruebaProgramacionFXML` | 19 | Correctas |
| `PruebaErrorSQLProgramacion` | 4 | Correctas |

Se verificaron altas, ediciones, horas contiguas, solapamiento por un minuto,
salas distintas, catálogos activos, fechas pasadas, precios inválidos,
conservación del historial, boletos activos y estados cancelado/finalizado.
La prueba de pantalla usa los controles reales, confirma las acciones y
comprueba la persistencia en MySQL; genera las tres capturas automáticamente.

La prueba `PruebaErrorSQLProgramacion` requiere una contraseña de MySQL
incorrecta en la configuración de la copia de pruebas. Tras compilar con esa
configuración, ejecútala con el mismo Classpath:

```bat
java -Dcine.pruebas.bd=true -cp "build\classes;build\test\classes;lib\*;lib\javafx-21.0.11\*" org.cine.test.PruebaErrorSQLProgramacion
```

Después restaura la configuración válida y vuelve a compilar. Esta prueba
comprueba que la vista explica el error, deshabilita Guardar, permite volver
a intentar la carga y conserva la navegación al dashboard.

También se ejecutaron las cuatro clases de pruebas anteriores del proyecto y
un arranque real con Ant. El login, la navegación y el cierre de sesión de
administrador, taquillero, bodega y cliente siguieron funcionando.

## Evidencia

- `evidencia/us_2_2/programacion_funciones.png`: formulario con horario válido.
- `evidencia/us_2_2/conflicto_horario.png`: explicación del solapamiento.
- `evidencia/us_2_2/cancelacion_finalizacion.png`: estados y finalización.
- `evidencia/us_2_2/RESULTADOS.txt`: salida de las comprobaciones ejecutadas.

Las tareas SQL T2.2.1 y T2.2.2 continúan pendientes. Las pruebas utilizaron
los scripts existentes sin modificar los archivos SQL del proyecto.
