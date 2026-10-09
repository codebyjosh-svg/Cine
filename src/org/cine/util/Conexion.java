
package org.cine.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Conexion {

    private static final Conexion INSTANCIA = new Conexion();

    private final Properties propiedades = new Properties();

    private Conexion() {
        try (InputStream entrada =
                Conexion.class.getResourceAsStream("/db.properties")) {

            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontro el archivo db.properties."
                );
            }

            propiedades.load(entrada);

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo leer el archivo db.properties.",
                    ex
            );
        }
    }

    public static Conexion getInstance() {
        return INSTANCIA;
    }

    public static Conexion getInstancia() {
        return INSTANCIA;
    }

    private String obtenerPropiedad(String clave) {
        String valor = propiedades.getProperty(clave);

        if (valor == null) {
            throw new IllegalStateException(
                    "Falta la propiedad " + clave
                    + " en db.properties."
            );
        }

        if (valor.isBlank() && !clave.equals("db.password")) {
            throw new IllegalStateException(
                    "La propiedad " + clave + " esta vacia."
            );
        }

        return valor;
    }

    public Connection getConnection() throws SQLException {

        String url = obtenerPropiedad("db.url");
        String usuario = obtenerPropiedad("db.user");
        String password = obtenerPropiedad("db.password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conexion = DriverManager.getConnection(
                    url,
                    usuario,
                    password
            );

            System.out.println(
                    "Conexion a MySQL establecida correctamente."
            );

            return conexion;

        } catch (ClassNotFoundException ex) {
            throw new SQLException(
                    "No se encontro el driver de MySQL. "
                    + "Verifica MySQL Connector/J.",
                    ex
            );

        } catch (SQLException ex) {
            System.err.println(
                    "Error al conectar con MySQL: "
                    + ex.getMessage()
            );

            throw ex;
        }
    }

    public Connection getConexion() throws SQLException {
        return getConnection();
    }

    public Connection conectar() throws SQLException {
        return getConnection();
    }
}
