package org.cine.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.cine.dao.UsuarioDAO;
import org.cine.model.ClienteVinculo;
import org.cine.model.Rol;
import org.cine.model.Usuario;
import org.cine.util.PasswordUtil;

public class UsuarioService {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{2,49}");
    private static final Pattern CORREO = Pattern.compile(
            "[A-Za-z0-9][A-Za-z0-9._%+\\-]*@[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?)+");
    private final UsuarioDAO dao;
    private Integer idUsuarioActual;

    public UsuarioService(UsuarioDAO dao) {
        this.dao = dao;
    }

    public void setIdUsuarioActual(Integer idUsuarioActual) {
        this.idUsuarioActual = idUsuarioActual;
    }

    public List<Usuario> listar() throws SQLException {
        return dao.listar();
    }

    public List<Rol> listarRoles() throws SQLException {
        return dao.listarRoles();
    }

    public List<ClienteVinculo> listarClientes() throws SQLException {
        return dao.listarClientes();
    }

    private String obligatorio(String valor, String campo, int maximo) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException("El campo " + campo + " permite hasta " + maximo + " caracteres.");
        }
        return limpio;
    }

    public int guardar(Usuario usuario, String contrasena, String confirmacion) throws SQLException {
        if (usuario == null || usuario.getIdUsuario() < 0) {
            throw new IllegalArgumentException("El usuario no es válido.");
        }
        usuario.setNombreUsuario(obligatorio(usuario.getNombreUsuario(), "nombre", 100));
        usuario.setApellidoUsuario(obligatorio(usuario.getApellidoUsuario(), "apellido", 100));
        usuario.setUsername(obligatorio(usuario.getUsername(), "username", 50));
        usuario.setCorreoElectronico(obligatorio(usuario.getCorreoElectronico(), "correo", 120)
                .toLowerCase(Locale.ROOT));
        if (!USERNAME.matcher(usuario.getUsername()).matches()) {
            throw new IllegalArgumentException("Username: usa de 3 a 50 letras, números, puntos, guiones o guion bajo.");
        }
        String correo = usuario.getCorreoElectronico();
        if (!CORREO.matcher(correo).matches() || correo.contains("..") || correo.contains(".@")) {
            throw new IllegalArgumentException("Escribe un correo válido, sin espacios ni un punto antes de @.");
        }
        Rol rol = null;
        for (Rol disponible : dao.listarRoles()) {
            if (disponible.getIdRol() == usuario.getIdRol()) {
                rol = disponible;
                break;
            }
        }
        if (rol == null) {
            throw new IllegalArgumentException("Selecciona un rol válido de la base de datos.");
        }
        String nombreRol = rol.getNombreRol() == null ? "" : rol.getNombreRol().trim().toLowerCase(Locale.ROOT);
        if (!java.util.Set.of("admin", "taquillero", "bodega", "cliente").contains(nombreRol)) {
            throw new IllegalArgumentException("Solo se permiten admin, taquillero, bodega y cliente.");
        }
        usuario.setNombreRol(rol.getNombreRol());
        Usuario anterior = null;
        if (usuario.getIdUsuario() != 0) {
            anterior = dao.buscarPorId(usuario.getIdUsuario()).orElse(null);
            if (anterior == null) {
                throw new IllegalArgumentException("El usuario ya no existe. Actualiza la lista.");
            }
        }
        if (anterior != null && esActual(usuario.getIdUsuario()) && anterior.getIdRol() != usuario.getIdRol()) {
            throw new IllegalArgumentException("No puedes cambiar tu propio rol durante la sesión.");
        }
        if ("cliente".equals(nombreRol)) {
            if (usuario.getIdCliente() == null) {
                throw new IllegalArgumentException("Selecciona el cliente que estará vinculado a esta cuenta.");
            }
            ClienteVinculo cliente = null;
            for (ClienteVinculo disponible : dao.listarClientes()) {
                if (java.util.Objects.equals(disponible.getIdCliente(), usuario.getIdCliente())) {
                    cliente = disponible;
                    break;
                }
            }
            if (cliente == null) {
                throw new IllegalArgumentException("El cliente seleccionado no existe.");
            }
            if (!cliente.isEstado() && (anterior == null || !usuario.getIdCliente().equals(anterior.getIdCliente()))) {
                throw new IllegalArgumentException("Selecciona un cliente activo para vincular la cuenta.");
            }
        } else {
            usuario.setIdCliente(null);
        }
        String clave = contrasena == null ? "" : contrasena;
        String repetir = confirmacion == null ? "" : confirmacion;
        if (anterior == null || !clave.isEmpty() || !repetir.isEmpty()) {
            if (clave.length() < 8 || clave.length() > 128 || clave.isBlank()) {
                throw new IllegalArgumentException("La contraseña debe tener entre 8 y 128 caracteres.");
            }
            if (!clave.equals(repetir)) {
                throw new IllegalArgumentException("Las contraseñas no coinciden.");
            }
        }
        for (Usuario existente : dao.listar()) {
            if (existente.getIdUsuario() == usuario.getIdUsuario()) {
                continue;
            }
            if (existente.getUsername().equalsIgnoreCase(usuario.getUsername())) {
                throw new IllegalArgumentException("El username ya está registrado.");
            }
            if (existente.getCorreoElectronico().equalsIgnoreCase(correo)) {
                throw new IllegalArgumentException("El correo ya está registrado.");
            }
            if (usuario.getIdCliente() != null && usuario.getIdCliente().equals(existente.getIdCliente())) {
                throw new IllegalArgumentException("El cliente ya tiene una cuenta vinculada.");
            }
        }
        usuario.setContrasenaHash(clave.isEmpty() ? null : PasswordUtil.hash(clave));
        if (anterior == null) {
            int id = dao.insertar(usuario);
            usuario.setIdUsuario(id);
            usuario.setEstado(true);
            return id;
        }
        dao.actualizar(usuario);
        return usuario.getIdUsuario();
    }

    private boolean esActual(int idUsuario) {
        return idUsuarioActual != null && idUsuarioActual == idUsuario;
    }

    public void cambiarEstado(int idUsuario, boolean activo) throws SQLException {
        Usuario usuario = dao.buscarPorId(idUsuario).orElse(null);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario ya no existe.");
        }
        if (!activo && esActual(idUsuario)) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta durante la sesión.");
        }
        if (usuario.isEstado() != activo) {
            dao.cambiarEstado(idUsuario, activo);
        }
    }

    public void eliminar(int idUsuario) throws SQLException {
        if (esActual(idUsuario)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta durante la sesión.");
        }
        if (dao.buscarPorId(idUsuario).isEmpty()) {
            throw new IllegalArgumentException("El usuario ya no existe.");
        }
        dao.eliminar(idUsuario);
    }
}
