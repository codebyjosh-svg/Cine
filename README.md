# Cinema - Sistema de Gestión Integral para Cine

Para ejecutar esta copia en NetBeans, consulta `LEEME_JAVAFX_CORREGIDO.txt`.
Las bibliotecas están incluidas en `lib/` con rutas relativas; la clase
principal es `org.cine.system.Lanzador` y no necesita opciones de VM.

El módulo de programación está disponible en **Administrador → Funciones**.
Consulta `TAREAS_US_2_2_JAVA.md` para las tareas Java implementadas, el uso
y las pruebas. Las tareas SQL T2.2.1 y T2.2.2 permanecen pendientes.

## Descripción

Cinema es un sistema de escritorio desarrollado para gestionar los principales procesos administrativos y operativos de un cine.

El sistema busca centralizar la información de películas, clientes, salas, funciones, ventas y productos de dulcería en una sola aplicación.

## Objetivo

Desarrollar una aplicación que permita administrar y controlar las operaciones principales de un cine de forma organizada, reduciendo errores y facilitando el acceso a la información.

## Tecnologías

- Java 21
- JavaFX
- FXML
- MySQL
- JDBC
- MVC
- DAO
- Git
- GitHub
- NetBeans

## Arquitectura

El proyecto utiliza una estructura basada en MVC complementada con DAO y servicios.

Flujo general:

FXML
    ↓
Controller
    ↓
Service
    ↓
DAO
    ↓
JDBC
    ↓
MySQL

## Estructura del proyecto

src/
└── org/
    └── cine/
        ├── controller/
        ├── dao/
        │   └── impl/
        ├── model/
        ├── service/
        ├── system/
        ├── util/
        └── view/
            └── style/

## Equipo Scrum

### Product Owner
Profesor

### Scrum Master
Emilio

### Desarrolladores
- Joshua
- Diego
- Manuel

## Roles del sistema

### Administrador
Gestiona la información principal del sistema y tiene acceso a las funciones administrativas.

### Taquillero
Realiza ventas de boletos y atiende a los clientes.

### Encargado de sala
Gestiona y consulta la información relacionada con salas, asientos y funciones.

### Encargado de dulcería
Gestiona productos de comida y el inventario de dulcería.

## Épicas

### Épica 1 - Administración y configuración del cine
Incluye la gestión de usuarios, películas y clientes.

### Épica 2 - Operación y ventas del cine
Incluye la gestión de salas, asientos, funciones y venta de boletos.

### Épica 3 - Control administrativo y mejora del sistema
Incluye la gestión de dulcería, inventario, dashboards y reportes.

## Sprints

### Sprint 1 - Administración y configuración del cine
Desarrollo de la base administrativa del sistema, usuarios, películas y clientes.

### Sprint 2 - Operación y ventas del cine
Desarrollo de salas, asientos, funciones y venta de boletos.

### Sprint 3 - Control administrativo y mejora del sistema
Desarrollo de dulcería, inventario, dashboards, reportes e integración final.

## Estrategia de Git

La rama `main` contendrá las versiones estables del proyecto.

La rama `develop` será utilizada para integrar el trabajo realizado durante cada Sprint.

Cada desarrollador trabajará en una rama propia creada a partir de `develop`.

Flujo de trabajo:

feature
    ↓
Pull Request
    ↓
Revisión
    ↓
Correcciones
    ↓
develop
    ↓
Pruebas de integración
    ↓
main

## Reglas de trabajo

- No trabajar directamente sobre `main`.
- Las ramas de funcionalidades deben partir de `develop`.
- Cada cambio debe tener commits claros.
- Antes de integrar una rama se debe realizar una revisión.
- Al finalizar cada Sprint se realizará la integración de las funcionalidades en `develop`.
- Se realizarán pruebas después de la integración.

## Estado del proyecto

El proyecto incluye el login y los dashboards de US-1.1 y la gestión de usuarios de Diego (US-1.2).

El administrador puede abrir Usuarios desde su dashboard para guardar, editar, activar/desactivar y eliminar cuentas. El botón Volver regresa al dashboard. Los demás roles conservan sus pantallas.

Consulta LEEME_US_1_2.txt para configurar y ejecutar el proyecto en NetBeans. Las clases están en src/, los scripts en sql/ y las pruebas y capturas en test/. La clase principal es org.cine.system.Principal.


## US-3.3 — Stock crítico y venta de dulcería

Se integraron las tareas T3.3.1 a T3.3.12 de Joshua: consulta y alertas de stock
crítico, catálogo de dulcería en ventas, cantidades acumuladas por producto,
validación de existencias, confirmación transaccional y factura con subtotales.

Para una BD existente, ejecute `sql/actualizacion_US_3_3_stock_dulceria.sql`.
Para abrir y utilizar el proyecto, consulte `LEEME_US_3_3.txt`.
Las tareas están documentadas en `documentacion/US_3_3_Joshua.md` y la evidencia
se encuentra en `test/evidencia/US_3_3/`.
