package org.cine.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Conexion {
    private static Conexion instancia;
    private final String url;
    private final String user;
    private final String password;

    private Conexion() {
        Properties propiedades = new Properties();
        try (InputStream entrada = Conexion.class.getResourceAsStream("/db.properties")) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontró db.properties.");
            }
            propiedades.load(entrada);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo cargar db.properties.", ex);
        }
        url = propiedades.getProperty("db.url");
        user = propiedades.getProperty("db.user");
        password = propiedades.getProperty("db.password", "");
    }

    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
