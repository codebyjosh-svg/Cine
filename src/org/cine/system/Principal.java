package org.cine.system;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.cine.controller.FacturaController;
import org.cine.model.Usuario;
import org.cine.service.NavegacionRol;
import org.cine.service.PermisosVenta;
import org.cine.service.SesionContext;

/**
 * Clase principal de la aplicación Cinema.
 *
 * Se encarga de:
 * - Iniciar JavaFX.
 * - Mostrar el login.
 * - Navegar entre las diferentes vistas.
 * - Controlar el acceso según el rol.
 * - Mostrar ventas y facturas.
 * - Administrar el cambio de escenas.
 */
public class Principal extends Application {

    private static Stage stagePrincipal;

    // =========================================================
    // INICIO DE LA APLICACIÓN
    // =========================================================

    @Override
    public void start(Stage stage) throws IOException {

        stagePrincipal = stage;

        stagePrincipal.setMinWidth(860);
        stagePrincipal.setMinHeight(540);

        mostrarLogin();

        stagePrincipal.show();
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public static void mostrarLogin()
            throws IOException {

        cambiarEscena(
                "/org/cine/view/Login.fxml",
                "Cine - Inicio de sesión"
        );
    }

    // =========================================================
    // DASHBOARD SEGÚN ROL
    // =========================================================

    public static void mostrarDashboardSegunRol()
            throws IOException {

        if (!SesionContext.haySesionActiva()) {

            mostrarLogin();

            return;
        }

        String ruta =
                NavegacionRol.vistaPorRol(
                        SesionContext.getRolActual()
                );

        cambiarEscena(
                ruta,
                "Cine - "
                + SesionContext.getRolActual()
        );
    }

    // =========================================================
    // USUARIOS
    // =========================================================

    public static void mostrarUsuarios()
            throws IOException {

        Usuario actual =
                SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(
                        actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de usuarios requiere "
                    + "una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Usuarios.fxml",
                "Cine - Usuarios"
        );
    }

    // =========================================================
    // PELÍCULAS
    // =========================================================

    public static void mostrarPeliculas()
            throws IOException {

        Usuario actual =
                SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(
                        actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de películas requiere "
                    + "una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Peliculas.fxml",
                "Cine - Películas"
        );
    }

    // =========================================================
    // GÉNEROS
    // =========================================================

    public static void mostrarGeneros()
            throws IOException {

        Usuario actual =
                SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(
                        actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de géneros requiere "
                    + "una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Generos.fxml",
                "Cine - Géneros"
        );
    }

    // =========================================================
    // CLIENTES
    // =========================================================

    /**
     * US-1.4 - Gestión de clientes.
     */
    public static void mostrarClientes()
            throws IOException {

        Usuario actual =
                SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(
                        actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de clientes requiere "
                    + "una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Clientes.fxml",
                "Cine - Clientes"
        );
    }

    // =========================================================
    // SALAS Y BUTACAS - US-2.1
    // =========================================================

    /**
     * US-2.1 - Gestión de salas y butacas.
     */
    public static void mostrarSalas()
            throws IOException {

        Usuario actual =
                SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(
                        actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de salas requiere "
                    + "una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Salas.fxml",
                "Cine - Salas y butacas"
        );
    }

    // =========================================================
    // VENTA DE BOLETOS - US-2.4
    // =========================================================

    public static void mostrarVenta()
            throws IOException {

        String rol =
                SesionContext.getRolActual();

        if (rol == null
                || !("admin".equalsIgnoreCase(rol)
                || "taquillero".equalsIgnoreCase(rol))) {

            throw new IllegalStateException(
                    "No autorizado para ventas."
            );
        }

        cambiarEscena(
                "/org/cine/view/Venta.fxml",
                "Cinema - Venta de boletos"
        );
    }

    // =========================================================
    // VENTA CON FUNCIÓN PRESELECCIONADA
    // US-2.3 -> US-2.4
    // =========================================================

    public static void mostrarVenta(
            int idFuncion)
            throws IOException {

        String rol =
                SesionContext.getRolActual();

        if (rol == null
                || !("admin".equalsIgnoreCase(rol)
                || "taquillero".equalsIgnoreCase(rol))) {

            throw new IllegalStateException(
                    "No autorizado para ventas."
            );
        }

        FXMLLoader loader =
                new FXMLLoader(
                        Principal.class.getResource(
                                "/org/cine/view/Venta.fxml"
                        )
                );

        Parent root =
                loader.load();

        org.cine.controller.VentaController controller =
                loader.getController();

        /*
         * Se pasa la función seleccionada
         * desde Cartelera.
         */
        controller.seleccionarFuncion(
                idFuncion
        );

        Scene scene =
                new Scene(
                        root,
                        1100,
                        700
                );

        stagePrincipal.setScene(scene);

        stagePrincipal.setTitle(
                "Cinema - Venta de boletos"
        );

        stagePrincipal.setMinWidth(1000);
        stagePrincipal.setMinHeight(650);

        stagePrincipal.sizeToScene();
        stagePrincipal.centerOnScreen();
    }

    // =========================================================
    // FACTURA - US-2.4
    // =========================================================

    public static void mostrarFactura(
            int idVenta)
            throws IOException {

        /*
         * Mantiene la validación de permisos
         * utilizada por la integración de ventas.
         */
        PermisosVenta.exigirVendedor();

        FXMLLoader loader =
                new FXMLLoader(
                        Principal.class.getResource(
                                "/org/cine/view/Factura.fxml"
                        )
                );

        Parent root =
                loader.load();

        aplicarEscena(
                root,
                "Cine - Factura #" + idVenta,
                true,
                true
        );

        /*
         * Se utiliza cargarFactura() porque es
         * la implementación de FacturaController
         * que viene del lado HEAD.
         */
        FacturaController controller =
                loader.getController();

        controller.cargarFactura(
                idVenta
        );
    }

    // =========================================================
    // PROGRAMACIÓN - US-2.2
    // =========================================================

    /**
     * US-2.2 - Programación de funciones.
     */
    public static void mostrarProgramacion()
            throws IOException {

        Usuario actual =
                SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(
                        actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La programación de funciones requiere "
                    + "una sesión de administrador."
            );
        }

        cambiarEscena(
                "/org/cine/view/Programacion.fxml",
                "Cine - Programación de funciones"
        );
    }

    // =========================================================
    // CARTELERA - US-2.3
    // =========================================================

    /**
     * US-2.3 - Consulta de cartelera.
     */
    public static void mostrarCartelera()
            throws IOException {

        String rol =
                SesionContext.getRolActual();

        if (rol == null
                || !("admin".equalsIgnoreCase(rol)
                || "taquillero".equalsIgnoreCase(rol))) {

            throw new IllegalStateException(
                    "No autorizado para consultar "
                    + "la cartelera."
            );
        }

        cambiarEscena(
                "/org/cine/view/Cartelera.fxml",
                "Cine - Cartelera"
        );
    }

    // =========================================================
    // STOCK CRÍTICO
    // =========================================================

    public static void mostrarStockCritico()
            throws IOException {

        PermisosVenta.exigirStockCritico();

        cambiarEscena(
                "/org/cine/view/StockCritico.fxml",
                "Cine - Stock crítico"
        );
    }

    // =========================================================
    // VENTAS / DULCERÍA
    // =========================================================

    public static void mostrarVentas()
            throws IOException {

        PermisosVenta.exigirVendedor();

        cambiarEscena(
                "/org/cine/view/Venta.fxml",
                "Cine - Ventas"
        );
    }

    // =========================================================
    // CERRAR SESIÓN
    // =========================================================

    public static void cerrarSesion()
            throws IOException {

        SesionContext.cerrarSesion();

        mostrarLogin();
    }

    // =========================================================
    // CAMBIO DE ESCENA
    // =========================================================

    public static void cambiarEscena(
            String ruta,
            String titulo)
            throws IOException {

        URL recurso =
                Principal.class.getResource(
                        ruta
                );

        if (recurso == null) {

            throw new IOException(
                    "No se encontró la vista: "
                    + ruta
            );
        }

        FXMLLoader loader =
                new FXMLLoader(
                        recurso
                );

        Parent root =
                loader.load();

        // =====================================================
        // IDENTIFICAR VISTAS
        // =====================================================

        boolean usuarios =
                ruta.endsWith(
                        "/Usuarios.fxml"
                );

        boolean peliculas =
                ruta.endsWith(
                        "/Peliculas.fxml"
                );

        boolean generos =
                ruta.endsWith(
                        "/Generos.fxml"
                );

        boolean clientes =
                ruta.endsWith(
                        "/Clientes.fxml"
                );

        boolean salas =
                ruta.endsWith(
                        "/Salas.fxml"
                );

        boolean venta =
                ruta.endsWith(
                        "/Venta.fxml"
                );

        boolean factura =
                ruta.endsWith(
                        "/Factura.fxml"
                );

        boolean stockCritico =
                ruta.endsWith(
                        "/StockCritico.fxml"
                );

        boolean programacion =
                ruta.endsWith(
                        "/Programacion.fxml"
                );

        boolean cartelera =
                ruta.endsWith(
                        "/Cartelera.fxml"
                );

        // =====================================================
        // MÓDULOS GRANDES
        // =====================================================

        boolean moduloGrande =
                usuarios
                || peliculas
                || generos
                || clientes
                || salas
                || venta
                || factura
                || stockCritico
                || programacion
                || cartelera;

        // =====================================================
        // OPERACIONES
        // =====================================================

        boolean operacion =
                venta
                || factura
                || stockCritico;

        aplicarEscena(
                root,
                titulo,
                moduloGrande,
                operacion
        );
    }

    // =========================================================
    // CONFIGURAR ESCENA
    // =========================================================

    private static void aplicarEscena(
            Parent root,
            String titulo,
            boolean grande,
            boolean operacion) {

        var pantalla =
                Screen.getPrimary()
                        .getVisualBounds();

        /*
         * Se evita que la ventana salga de la pantalla.
         */
        double ancho =
                Math.min(
                        operacion
                                ? 1180
                                : grande
                                ? 1100
                                : 860,
                        pantalla.getWidth() - 48
                );

        double alto =
                Math.min(
                        operacion
                                ? 720
                                : grande
                                ? 700
                                : 540,
                        pantalla.getHeight() - 48
                );

        stagePrincipal.setMinWidth(
                Math.min(
                        grande
                                ? 1050
                                : 860,
                        ancho
                )
        );

        stagePrincipal.setMinHeight(
                Math.min(
                        grande
                                ? 650
                                : 540,
                        alto
                )
        );

        stagePrincipal.setScene(
                new Scene(
                        root,
                        ancho,
                        alto
                )
        );

        stagePrincipal.setTitle(
                titulo
        );

        stagePrincipal.sizeToScene();

        stagePrincipal.centerOnScreen();
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args) {

        launch(args);
    }
}