package org.cine.test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.model.Rol;
import org.cine.model.Usuario;
import org.cine.service.UsuarioService;
import org.cine.util.Conexion;
import org.cine.util.PasswordUtil;
import org.cine.service.AutenticacionException;

public class PruebaUsuarioCRUD {
    @FunctionalInterface
    private interface Operacion { void ejecutar() throws Exception; }

    private static int comprobaciones;
    private static final String CLAVE = "PruebaDiego2026!";
    private static final UsuarioDAOImpl DAO = new UsuarioDAOImpl();
    private static final UsuarioService SERVICIO = new UsuarioService(DAO);

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) { throw new AssertionError(mensaje); }
        comprobaciones++;
        System.out.println("OK " + comprobaciones + " · " + mensaje);
    }

    private static void rechaza(Class<? extends Exception> tipo, Operacion operacion, String mensaje) throws Exception {
        try {
            operacion.ejecutar();
        } catch (Exception ex) {
            if (!tipo.isInstance(ex)) { throw ex; }
            comprobar(true, mensaje);
            return;
        }
        throw new AssertionError("Se aceptó un caso inválido: " + mensaje);
    }

    private static Usuario nuevo(String username, int rol) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario("Diego");
        usuario.setApellidoUsuario("Pruebas");
        usuario.setUsername(username);
        usuario.setCorreoElectronico(username + "@example.com");
        usuario.setIdRol(rol);
        return usuario;
    }

    private static String hashBD(int id) throws SQLException {
        try (Connection conexion = Conexion.getInstance().getConnection();
             PreparedStatement sentencia = conexion.prepareStatement("SELECT contrasena_hash FROM usuarios WHERE id_usuario=?")) {
            sentencia.setInt(1, id);
            try (ResultSet datos = sentencia.executeQuery()) {
                if (!datos.next()) { throw new AssertionError("Usuario no encontrado."); }
                return datos.getString(1);
            }
        }
    }

    private static int crearCliente(String correo, boolean activo) throws SQLException {
        try (Connection conexion = Conexion.getInstance().getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(
                     "INSERT INTO clientes(nombre_cliente,apellido_cliente,correo_electronico,estado) VALUES('Cliente','Pruebas',?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, correo);
            sentencia.setBoolean(2, activo);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (!Boolean.getBoolean("cine.pruebas.bd")) {
            throw new IllegalStateException("Configura una BD de pruebas y agrega -Dcine.pruebas.bd=true para ejecutar.");
        }
        String sufijo = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        List<Integer> idsUsuarios = new ArrayList<>();
        List<Integer> idsClientes = new ArrayList<>();
        List<Rol> roles = DAO.listarRoles();
        int taquilla = roles.stream().filter(r -> r.getNombreRol().equals("taquillero")).findFirst().orElseThrow().getIdRol();
        int bodega = roles.stream().filter(r -> r.getNombreRol().equals("bodega")).findFirst().orElseThrow().getIdRol();
        int cliente = roles.stream().filter(r -> r.getNombreRol().equals("cliente")).findFirst().orElseThrow().getIdRol();
        int inicial = DAO.listar().size();
        try {
            comprobar(roles.size() == 4, "Se cargan los cuatro roles de la BD");
            String seed = "pbkdf2_sha256$600000$AiKMiHY7ai32oCFgL51UzA==$/hRH2SPMrMNuXnULrVM5aP1eTmFjV9wGMcqKFr3YYDs=";
            comprobar(PasswordUtil.verificar("AdminCine2026!", seed), "PBKDF2 compatible con el DML compartido");
            comprobar(!PasswordUtil.verificar("incorrecta", seed), "Contraseña incorrecta rechazada");
            comprobar(!PasswordUtil.verificar(CLAVE, "pbkdf2_sha256$texto$no$no"), "Hash malformado rechazado");
            Usuario usuario = nuevo("prueba.us12." + sufijo, taquilla);
            int id = SERVICIO.guardar(usuario, CLAVE, CLAVE);
            idsUsuarios.add(id);
            comprobar(id > 0, "INSERT devuelve un ID real");
            Usuario guardado = DAO.buscarPorId(id).orElseThrow();
            comprobar(guardado.isEstado() && guardado.getFechaRegistro() != null, "Estado activo y fecha de registro recuperados");
            comprobar(guardado.getIdCliente() == null && "taquillero".equals(guardado.getNombreRol()), "Rol y cliente NULL leídos correctamente");
            comprobar(guardado.getContrasenaHash() == null, "El listado administrativo no expone hashes");
            String hashInicial = hashBD(id);
            comprobar(PasswordUtil.verificar(CLAVE, hashInicial), "La contraseña se almacena como PBKDF2 válido");
            comprobar(DAO.listar().size() == inicial + 1, "LISTAR incluye el usuario nuevo");
            comprobar(DAO.buscarPorId(Integer.MAX_VALUE).isEmpty(), "BUSCAR inexistente devuelve Optional vacío");

            Usuario duplicado = nuevo(usuario.getUsername().toUpperCase(), taquilla);
            duplicado.setCorreoElectronico("otro." + sufijo + "@example.com");
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(duplicado, CLAVE, CLAVE), "Username duplicado sin importar mayúsculas");
            duplicado.setContrasenaHash(hashInicial);
            rechaza(SQLException.class, () -> DAO.insertar(duplicado), "MySQL también impide username duplicado");
            Usuario correoDuplicado = nuevo("correo." + sufijo, taquilla);
            correoDuplicado.setCorreoElectronico(usuario.getCorreoElectronico().toUpperCase());
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(correoDuplicado, CLAVE, CLAVE), "Correo duplicado rechazado por el servicio");
            correoDuplicado.setContrasenaHash(hashInicial);
            rechaza(SQLException.class, () -> DAO.insertar(correoDuplicado), "MySQL también impide correo duplicado");

            for (String correo : List.of("nombre.@example.com", "nombre..apellido@example.com", "sin-arroba", "a@dominio", "a b@example.com")) {
                Usuario invalido = nuevo("invalido." + sufijo, taquilla);
                invalido.setCorreoElectronico(correo);
                rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(invalido, CLAVE, CLAVE), "Correo inválido: " + correo);
            }
            for (String username : List.of("ab", "usuario con espacios", "@usuario")) {
                Usuario invalido = nuevo(username, taquilla);
                rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(invalido, CLAVE, CLAVE), "Username inválido: " + username);
            }
            Usuario sinNombre = nuevo("vacio." + sufijo, taquilla);
            sinNombre.setNombreUsuario("  ");
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(sinNombre, CLAVE, CLAVE), "Nombre obligatorio");
            Usuario sinApellido = nuevo("vacio." + sufijo, taquilla);
            sinApellido.setApellidoUsuario("");
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(sinApellido, CLAVE, CLAVE), "Apellido obligatorio");
            Usuario sinRol = nuevo("sinrol." + sufijo, 0);
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(sinRol, CLAVE, CLAVE), "Rol inexistente rechazado");
            sinRol.setContrasenaHash(hashInicial);
            rechaza(SQLException.class, () -> DAO.insertar(sinRol), "El procedimiento valida el rol");
            Usuario claveCorta = nuevo("clave." + sufijo, taquilla);
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(claveCorta, "123", "123"), "Contraseña corta rechazada");
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(claveCorta, CLAVE, "otra-clave"), "Confirmación de contraseña validada");

            guardado.setNombreUsuario("Diego editado");
            guardado.setIdRol(bodega);
            SERVICIO.guardar(guardado, "", "");
            comprobar("Diego editado".equals(DAO.buscarPorId(id).orElseThrow().getNombreUsuario()), "ACTUALIZAR persiste los datos");
            comprobar("bodega".equals(DAO.buscarPorId(id).orElseThrow().getNombreRol()), "Cambio de rol persistido");
            comprobar(hashInicial.equals(hashBD(id)), "Editar sin contraseña conserva el hash original");
            String nombrePersistido = guardado.getNombreUsuario();
            guardado.setNombreUsuario("Cambio que debe revertirse");
            guardado.setContrasenaHash("hash_invalido");
            Usuario cambioFallido = guardado;
            rechaza(SQLException.class, () -> DAO.actualizar(cambioFallido), "Fallo al cambiar contraseña provoca rollback");
            comprobar(nombrePersistido.equals(DAO.buscarPorId(id).orElseThrow().getNombreUsuario()), "El rollback revierte también el nombre");
            guardado = DAO.buscarPorId(id).orElseThrow();
            SERVICIO.guardar(guardado, "ClaveNueva2026!", "ClaveNueva2026!");
            comprobar(PasswordUtil.verificar("ClaveNueva2026!", hashBD(id)), "Cambio de contraseña compatible con login");
            comprobar(!PasswordUtil.verificar(CLAVE, hashBD(id)), "La contraseña anterior deja de servir");

            SERVICIO.cambiarEstado(id, false);
            comprobar(!DAO.buscarPorId(id).orElseThrow().isEstado(), "DESACTIVAR persiste estado 0");
            comprobar(DAO.listar().stream().anyMatch(u -> u.getIdUsuario() == id && !u.isEstado()), "LISTAR conserva usuarios inactivos");
            try {
                DAO.autenticar(usuario.getUsername(), "ClaveNueva2026!");
                throw new AssertionError("Se esperaba rechazar al usuario inactivo.");
            } catch (AutenticacionException ex) {
                comprobar(ex.getMotivo() == AutenticacionException.Motivo.USUARIO_INACTIVO,
                        "El login integrado rechaza al usuario inactivo");
            }
            SERVICIO.cambiarEstado(id, true);
            comprobar(DAO.autenticar(usuario.getUsername(), "ClaveNueva2026!").getIdUsuario() == id,
                    "ACTIVAR vuelve a permitir el login");
            SERVICIO.setIdUsuarioActual(id);
            rechaza(IllegalArgumentException.class, () -> SERVICIO.cambiarEstado(id, false), "No se desactiva la cuenta de la sesión actual");
            rechaza(IllegalArgumentException.class, () -> SERVICIO.eliminar(id), "No se elimina la cuenta de la sesión actual");
            Usuario cuentaActual = DAO.buscarPorId(id).orElseThrow();
            cuentaActual.setIdRol(taquilla);
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(cuentaActual, "", ""), "No se cambia el propio rol durante la sesión");
            SERVICIO.setIdUsuarioActual(null);

            Usuario cuentaCliente = nuevo("cliente." + sufijo, cliente);
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(cuentaCliente, CLAVE, CLAVE), "Rol cliente requiere un vínculo");
            int idCliente = crearCliente("cliente." + sufijo + "@example.com", true);
            idsClientes.add(idCliente);
            int idInactivo = crearCliente("inactivo." + sufijo + "@example.com", false);
            idsClientes.add(idInactivo);
            cuentaCliente.setIdCliente(idInactivo);
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(cuentaCliente, CLAVE, CLAVE), "No se vincula un cliente inactivo nuevo");
            cuentaCliente.setIdCliente(idCliente);
            int idCuentaCliente = SERVICIO.guardar(cuentaCliente, CLAVE, CLAVE);
            idsUsuarios.add(idCuentaCliente);
            comprobar(DAO.buscarPorId(idCuentaCliente).orElseThrow().getIdCliente().equals(idCliente), "La cuenta cliente conserva su vínculo");
            Usuario clienteRepetido = nuevo("duplicado.cliente." + sufijo, cliente);
            clienteRepetido.setIdCliente(idCliente);
            rechaza(IllegalArgumentException.class, () -> SERVICIO.guardar(clienteRepetido, CLAVE, CLAVE), "Un cliente no puede tener dos cuentas");
            clienteRepetido.setContrasenaHash(hashInicial);
            rechaza(SQLException.class, () -> DAO.insertar(clienteRepetido), "La restricción UNIQUE protege el vínculo de cliente");
            cuentaCliente.setIdRol(bodega);
            SERVICIO.guardar(cuentaCliente, "", "");
            comprobar(DAO.buscarPorId(idCuentaCliente).orElseThrow().getIdCliente() == null, "Cambiar a un rol interno elimina el vínculo de cliente");
            SERVICIO.eliminar(idCuentaCliente);
            idsUsuarios.remove(Integer.valueOf(idCuentaCliente));
            SERVICIO.eliminar(id);
            idsUsuarios.remove(Integer.valueOf(id));
            comprobar(DAO.buscarPorId(id).isEmpty(), "ELIMINAR borra físicamente una cuenta sin historial");
            comprobar(DAO.listar().size() == inicial, "El número de usuarios vuelve al estado inicial");
            System.out.println("RESULTADO: " + comprobaciones + " comprobaciones correctas.");
        } finally {
            for (int id : idsUsuarios) { DAO.eliminar(id); }
            try (Connection conexion = Conexion.getInstance().getConnection();
                 PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM clientes WHERE id_cliente=?")) {
                for (int id : idsClientes) {
                    sentencia.setInt(1, id);
                    sentencia.executeUpdate();
                }
            }
        }
    }
}
