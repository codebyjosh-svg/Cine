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

    public static void mostrarLogin() throws IOException {

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

        validarAdministrador("usuarios");

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

        validarAdministrador("películas");

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

        validarAdministrador("géneros");

        cambiarEscena(
                "/org/cine/view/Generos.fxml",
                "Cine - Géneros"
        );
    }

    // =========================================================
    // CLIENTES
    // =========================================================

    public static void mostrarClientes()
            throws IOException {

        validarAdministrador("clientes");

        cambiarEscena(
                "/org/cine/view/Clientes.fxml",
                "Cine - Clientes"
        );
    }

    // =========================================================
    // PRODUCTOS DE DULCERÍA
    // =========================================================

    public static void mostrarProductos()
            throws IOException {

        validarAdministrador("productos");

        cambiarEscena(
                "/org/cine/view/Productos.fxml",
                "Cine - Productos de dulcería"
        );
    }

    // =========================================================
    // CATEGORÍAS DE PRODUCTOS
    // =========================================================

    public static void mostrarCategoriasProducto()
            throws IOException {

        validarAdministrador("categorías de productos");

        cambiarEscena(
                "/org/cine/view/CategoriasProducto.fxml",
                "Cine - Categorías de productos"
        );
    }

    // =========================================================
    // SALAS Y BUTACAS - US-2.1
    // =========================================================

    public static void mostrarSalas()
            throws IOException {

        validarAdministrador("salas");

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

    public static void mostrarVenta(int idFuncion)
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

        Parent root = loader.load();

        org.cine.controller.VentaController controller =
                loader.getController();

        controller.seleccionarFuncion(idFuncion);

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

    public static void mostrarFactura(int idVenta)
            throws IOException {

        PermisosVenta.exigirVendedor();

        FXMLLoader loader =
                new FXMLLoader(
                        Principal.class.getResource(
                                "/org/cine/view/Factura.fxml"
                        )
                );

        Parent root = loader.load();

        aplicarEscena(
                root,
                "Cine - Factura #" + idVenta,
                true,
                true
        );

        FacturaController controller =
                loader.getController();

        controller.cargarFactura(idVenta);
    }

    // =========================================================
    // PROGRAMACIÓN - US-2.2
    // =========================================================

    public static void mostrarProgramacion()
            throws IOException {

        validarAdministrador(
                "programación de funciones"
        );

        cambiarEscena(
                "/org/cine/view/Programacion.fxml",
                "Cine - Programación de funciones"
        );
    }

    // =========================================================
    // CARTELERA - US-2.3
    // =========================================================

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
    // VALIDACIÓN DE ADMINISTRADOR
    // =========================================================

    private static void validarAdministrador(
            String modulo) {

        Usuario actual =
                SesionContext.getUsuarioActual();

        if (actual == null
                || !actual.isEstado()
                || !"admin".equalsIgnoreCase(
                        actual.getNombreRol())) {

            throw new IllegalStateException(
                    "La gestión de " + modulo
                    + " requiere una sesión "
                    + "de administrador."
            );
        }
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

        if (stagePrincipal == null) {

            throw new IllegalStateException(
                    "La ventana principal todavía "
                    + "no está inicializada."
            );
        }

        if (ruta == null || ruta.isBlank()) {

            throw new IOException(
                    "No se indicó la ruta de la vista."
            );
        }

        URL recurso =
                Principal.class.getResource(ruta);

        if (recurso == null) {

            throw new IOException(
                    "No se encontró la vista: "
                    + ruta
            );
        }

        FXMLLoader loader =
                new FXMLLoader(recurso);

        Parent root = loader.load();

        // =====================================================
        // IDENTIFICAR VISTAS
        // =====================================================

        boolean usuarios =
                ruta.endsWith("/Usuarios.fxml");

        boolean peliculas =
                ruta.endsWith("/Peliculas.fxml");

        boolean generos =
                ruta.endsWith("/Generos.fxml");

        boolean clientes =
                ruta.endsWith("/Clientes.fxml");

        boolean productos =
                ruta.endsWith("/Productos.fxml");

        boolean categoriasProducto =
                ruta.endsWith(
                        "/CategoriasProducto.fxml"
                );

        boolean salas =
                ruta.endsWith("/Salas.fxml");

        boolean venta =
                ruta.endsWith("/Venta.fxml");

        boolean factura =
                ruta.endsWith("/Factura.fxml");

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
                || productos
                || categoriasProducto
                || salas
                || programacion;

        // =====================================================
        // PANTALLAS DE OPERACIÓN
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

        stagePrincipal.setTitle(titulo);

        stagePrincipal.sizeToScene();

        stagePrincipal.centerOnScreen();
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        launch(args);
    }
}