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
                        "No se encontró el archivo db.properties."
                );
            }

            propiedades.load(entrada);

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "No se pudo leer db.properties.",
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

    private String valor(
            String clave,
            String variable,
            String predeterminado) {

        String valor = System.getProperty(
                "cine." + clave
        );

        if (valor == null) {
            valor = System.getenv(variable);
        }

        if (valor == null) {
            valor = propiedades.getProperty(
                    clave,
                    predeterminado
            );
        }

        return valor;
    }

    public Connection getConnection() throws SQLException {

        try {

            Class.forName(
                    "com.mysql.cj.jdbc.Driver"
            );

        } catch (ClassNotFoundException ex) {

            throw new SQLException(
                    "Falta MySQL Connector/J en las bibliotecas del proyecto.",
                    ex
            );
        }

        String url = valor(
                "db.url",
                "CINE_DB_URL",
                "jdbc:mysql://localhost:3306/cinedb_in4cm"
                + "?useSSL=false"
                + "&serverTimezone=America/Guatemala"
        );

        String user = valor(
                "db.user",
                "CINE_DB_USER",
                "IN4CM"
        );

        String password = valor(
                "db.password",
                "CINE_DB_PASSWORD",
                ""
        );

        return DriverManager.getConnection(
                url,
                user,
                password
        );
    }

    public Connection getConexion() throws SQLException {
        return getConnection();
    }

    public Connection conectar() throws SQLException {
        return getConnection();
    }
}