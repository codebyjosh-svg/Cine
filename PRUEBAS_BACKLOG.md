# Verificación del backlog de Cine (12 historias)

## Sprint 1
- [ ] US-1.1: login correcto, contraseña incorrecta, cuenta inactiva, cerrar sesión, cada rol.
- [ ] US-1.2: alta/edición de usuarios, roles, username/correo duplicados, activar/desactivar.
- [ ] US-1.3: CRUD de géneros y películas, validaciones, relación género-película.
- [ ] US-1.4: CRUD clientes, CUI/correo repetidos, activación y búsqueda.

## Sprint 2
- [ ] US-2.1: CRUD salas y butacas, fila/número duplicados.
- [ ] US-2.2: funciones: sala y película activas, fechas, horarios cruzados, cancelar/finalizar.
- [ ] US-2.3: cartelera con resultados/vacío y navegación a venta.
- [ ] US-2.4: venta cliente obligatorio, butaca ocupada, transacción, comprobante y reconsulta.

## Sprint 3
- [ ] US-3.1: crear/editar/activar categorías y productos, categoría asignada, precios válidos.
- [ ] US-3.2: inventario entrada/salida, salida superior a stock bloqueada, historial con usuario.
- [ ] US-3.3: stock = mínimo y stock menor; agregar mismo producto dos veces en carrito; venta de varios productos; descuento y movimiento por cada línea; recuperar factura con todas las líneas.
- [ ] US-3.4: reportes por día/semana/mes y todos; KPI, total y filtros vacíos.

## Pruebas y cierre (requeridas antes de asegurar 100 %)
- [ ] Ejecutar procedimientos de la versión correcta de `sql/03_SP_Vistas_Cine.sql` en una BD de prueba.
- [ ] Ejecutar con MySQL real y capturar evidencias de cada caso normal y de error.
- [ ] Revisar las vistas FXML en Windows/Scene Builder, tamaños y navegación de todos los roles.
- [ ] Code review por un compañero y Pull Requests integrados en `develop`.
- [ ] Hacer pruebas de regresión sobre los módulos de los sprints 1 y 2.

**Seguridad de datos:** No ejecutes `01_DDL_Cine.sql` sobre una base con información real: contiene `DROP DATABASE`.
**Límite de verificación:** `BUILD SUCCESSFUL` confirma compilación pero no confirma los casos anteriores.
