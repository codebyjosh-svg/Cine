# Pruebas US-1.1

1. Login correcto: admin.cine / AdminCine2026!
2. Login incorrecto: admin.cine / cualquier contraseña incorrecta
3. Usuario inactivo: ejecutar `UPDATE usuarios SET estado = 0 WHERE username = 'admin.cine';`, intentar login y comprobar el mensaje de usuario inactivo. Restaurar con `UPDATE usuarios SET estado = 1 WHERE username = 'admin.cine';`.
4. Logout: iniciar sesión, presionar Cerrar sesión y comprobar que se regresa a Login.fxml y `SesionContext` queda vacío.

También se incluyen `PruebaSecurityUtil`, `PruebaNavegacionRol` y `PruebaLoginIntegracion`.
