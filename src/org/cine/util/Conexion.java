package org.cine.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {

    private static Conexion instancia;

    private String url;
    private String user;
    private String password;

    private Conexion() {

        try (InputStream entrada = Conexion.class.getResourceAsStream("/db.properties")) {

            Properties propiedades = new Properties();
            propiedades.load(entrada);

            url = propiedades.getProperty("db.url");
            user = propiedades.getProperty("db.user");
            password = propiedades.getProperty("db.password");

        } catch (Exception e) {
            System.err.println(
                    "Error al cargar db.properties: "
                    + e.getMessage()
            );
        }
    }

    public static Conexion getInstancia() {

        if (instancia == null) {
            instancia = new Conexion();
        }

        return instancia;
    }

    public Connection conectar() throws SQLException {

        return DriverManager.getConnection(
                url,
                user,
                password
        );
    }
}