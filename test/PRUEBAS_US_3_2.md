# Pruebas Java de inventario — T3.2.12

Verificado el 8 de octubre de 2026 con JDK 21.0.12, JavaFX 21.0.11,
MySQL 8.0.46, MySQL Connector/J 8.0.33 y Ant 1.10.15.
Las ventanas se probaron en Linux con JavaFX para Linux y Monocle en modo
sin monitor. La copia entregada incluye JavaFX para Windows x64 en Classpath.

## Preparación

Usa una copia desechable de la base con la estructura y los datos de ejemplo
de los SQL actuales. Configura `src/db.properties` para esa copia antes de
compilar. Las pruebas agregan y retiran una categoría, productos, un usuario
inactivo y movimientos de prueba. Las pruebas anteriores de login también
modifican temporalmente el estado del administrador.

Las pruebas requieren las cuentas de ejemplo del DML actual. El indicador
`-Dcine.pruebas.bd=true` confirma explícitamente que se usa una base de
pruebas. No ejecutes estos casos sobre una base con datos de uso real.

En una consola de Windows con JDK 21 y Ant, desde la carpeta del proyecto:

```bat
ant clean jar compile-test
java -Dcine.pruebas.bd=true -cp "build\classes;build\test\classes;lib\*;lib\javafx-21.0.11\*" org.cine.test.PruebaInventario
java -Dcine.pruebas.bd=true -cp "build\classes;build\test\classes;lib\*;lib\javafx-21.0.11\*" org.cine.test.LanzadorPruebaInventario
```

También puedes compilar con NetBeans y ejecutar las clases de prueba desde
el editor, agregando el indicador de pruebas a las opciones de esa ejecución.

## Cobertura

| Clase | Comprobaciones | Resultado |
| --- | ---: | --- |
| PruebaInventario | 28 | Correctas |
| PruebaInventarioFXML | 25 | Correctas |
| PruebaErrorSQLInventario | 4 | Correctas |

La integración JDBC verifica entrada, salida parcial, salida exacta,
stock cero, stock insuficiente, orden del historial, usuario, fecha y
observación. También comprueba productos y operadores inactivos, permisos,
longitud de observación y límites del stock entero.

Dos llamadas JDBC simultáneas intentan retirar 7 unidades de un producto
con stock 10. Solo una puede registrarse; quedan 3 unidades y una sola salida
en el historial. Esta prueba usa los procedimientos existentes sin modificarlos.

La prueba de interfaz opera los controles reales, verifica persistencia
en MySQL, búsqueda y filtros, acceso por rol, navegación, botones visibles
y mensajes de validación. Otro operador reduce el stock entre la carga del
formulario y el registro: la pantalla vuelve a leerlo y rechaza una salida
que ya no tiene existencias suficientes.

## Error de conexión

`PruebaErrorSQLInventario` requiere una contraseña de MySQL inválida en la
configuración de la copia de pruebas. Compila con esa configuración y ejecuta:

```bat
java -Dcine.pruebas.bd=true -cp "build\classes;build\test\classes;lib\*;lib\javafx-21.0.11\*" org.cine.test.PruebaErrorSQLInventario
```

Comprueba que la vista muestra el error, deshabilita Registrar, permite
intentar Actualizar y conserva Volver. Después restaura la configuración
válida y vuelve a compilar.

## Evidencia

- `evidencia/us_3_2/entrada_inventario.png`: entrada y stock aumentado.
- `evidencia/us_3_2/salida_inventario.png`: salida y stock disminuido.
- `evidencia/us_3_2/stock_insuficiente.png`: rechazo sin guardar cambios.
- `evidencia/us_3_2/historial_inventario.png`: movimientos de distintos usuarios.
- `evidencia/us_3_2/RESULTADOS.txt`: salida de las comprobaciones.

También pasaron las cuatro clases de pruebas anteriores del proyecto
y un arranque real con Ant que comprobó login y cierre de sesión de los
cuatro roles. El aviso de JavaFX sobre clases cargadas desde un módulo sin
nombre apareció al ejecutar desde Classpath y no impidió usar las pantallas.

Las tareas SQL T3.2.1 y T3.2.2 siguen pendientes. No se modificaron los
archivos SQL del proyecto ni la configuración original de conexión.
