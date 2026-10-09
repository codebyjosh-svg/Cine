package org.cine.test;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.cine.dao.impl.FuncionDAOImpl;
import org.cine.dao.impl.UsuarioDAOImpl;
import org.cine.model.Funcion;
import org.cine.model.Pelicula;
import org.cine.model.Sala;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

/** Prueba el flujo real de la pantalla y genera evidencia PNG. */
public class PruebaProgramacionFXML extends Application {
    private static volatile Throwable fallo;
    private final FuncionDAOImpl dao = new FuncionDAOImpl();
    private Stage escenario;
    private Timeline confirmar;
    private int comprobaciones;

    @Override
    public void start(Stage stage) throws Exception {
        escenario = stage;
        new Principal().start(stage);
        Platform.setImplicitExit(false);
        confirmar = new Timeline(new KeyFrame(javafx.util.Duration.millis(100), evento -> {
            for (Window ventana : List.copyOf(Window.getWindows())) {
                if (ventana != escenario && ventana.isShowing() && ventana.getScene().getRoot() instanceof DialogPane panel
                        && panel.lookupButton(ButtonType.YES) instanceof Button boton) boton.fire();
            }
        }));
        confirmar.setCycleCount(Timeline.INDEFINITE);
        confirmar.play();
        Thread hilo = new Thread(() -> {
            try { probar(); }
            catch (Throwable ex) { fallo = ex; ex.printStackTrace(); }
            finally {
                Platform.runLater(() -> {
                    confirmar.stop();
                    SesionContext.cerrarSesion();
                    escenario.close();
                    Platform.exit();
                });
            }
        }, "pruebas-programacion");
        hilo.setDaemon(true);
        hilo.start();
    }

    private <T> T fx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private void comprobar(boolean valor, String mensaje) {
        if (!valor) throw new AssertionError(mensaje);
        System.out.println("OK " + (++comprobaciones) + " · " + mensaje);
    }

    private void pulsar(String id) throws Exception {
        fx(() -> { ((Button) escenario.getScene().lookup("#" + id)).fire(); return null; });
    }

    private String mensaje() throws Exception {
        return fx(() -> ((Label) escenario.getScene().lookup("#lblMensaje")).getText());
    }

    @SuppressWarnings("unchecked")
    private void formulario(DatosFuncionesPrueba datos, LocalDateTime inicio, String precio) throws Exception {
        fx(() -> {
            ComboBox<Pelicula> peliculas = (ComboBox<Pelicula>) escenario.getScene().lookup("#cmbPelicula");
            ComboBox<Sala> salas = (ComboBox<Sala>) escenario.getScene().lookup("#cmbSala");
            peliculas.setValue(peliculas.getItems().stream().filter(p -> p.getIdPelicula() == datos.pelicula1).findFirst().orElseThrow());
            salas.setValue(salas.getItems().stream().filter(s -> s.getIdSala() == datos.sala1).findFirst().orElseThrow());
            DatePicker fecha = (DatePicker) escenario.getScene().lookup("#dpFecha");
            fecha.setValue(inicio.toLocalDate());
            fecha.getEditor().setText(DateTimeFormatter.ofPattern("dd/MM/uuuu").format(inicio));
            ((TextField) escenario.getScene().lookup("#txtHora")).setText(DateTimeFormatter.ofPattern("HH:mm").format(inicio));
            ((TextField) escenario.getScene().lookup("#txtPrecio")).setText(precio);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private void seleccionar(int id) throws Exception {
        fx(() -> {
            TableView<Funcion> tabla = (TableView<Funcion>) escenario.getScene().lookup("#tblFunciones");
            Funcion funcion = tabla.getItems().stream().filter(f -> f.getIdFuncion() == id).findFirst().orElseThrow();
            tabla.getSelectionModel().select(funcion);
            tabla.scrollTo(funcion);
            return null;
        });
    }

    private void capturar(String nombre) throws Exception {
        WritableImage imagen = fx(() -> {
            escenario.getScene().getRoot().applyCss();
            escenario.getScene().getRoot().layout();
            return escenario.getScene().snapshot(null);
        });
        BufferedImage archivo = new BufferedImage((int) imagen.getWidth(), (int) imagen.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < archivo.getHeight(); y++) {
            for (int x = 0; x < archivo.getWidth(); x++) archivo.setRGB(x, y, imagen.getPixelReader().getArgb(x, y));
        }
        Path destino = Path.of("test/evidencia/us_2_2", nombre);
        Files.createDirectories(destino.getParent());
        ImageIO.write(archivo, "png", destino.toFile());
    }

    @SuppressWarnings("unchecked")
    private void probar() throws Exception {
        try (DatosFuncionesPrueba datos = new DatosFuncionesPrueba()) {
            comprobar(fx(() -> {
                try { Principal.mostrarProgramacion(); return false; }
                catch (IllegalStateException ex) { return true; }
            }), "Programación bloqueada sin sesión de administrador");
            SesionContext.iniciarSesion(new UsuarioDAOImpl().autenticar("taquilla.cine", "TaquillaCine2026!"));
            comprobar(fx(() -> {
                try { Principal.mostrarProgramacion(); return false; }
                catch (IllegalStateException ex) { return true; }
            }), "Programación bloqueada para un taquillero autenticado");
            SesionContext.iniciarSesion(new UsuarioDAOImpl().autenticar("admin.cine", "AdminCine2026!"));
            fx(() -> { Principal.mostrarDashboardSegunRol(); return null; });
            pulsar("btnFunciones");
            comprobar(fx(() -> escenario.getScene().lookup("#tblFunciones") != null), "El botón Funciones abre Programacion.fxml");
            comprobar(fx(() -> {
                ComboBox<Pelicula> peliculas = (ComboBox<Pelicula>) escenario.getScene().lookup("#cmbPelicula");
                ComboBox<Sala> salas = (ComboBox<Sala>) escenario.getScene().lookup("#cmbSala");
                return peliculas.getItems().stream().noneMatch(p -> p.getIdPelicula() == datos.peliculaInactiva)
                        && salas.getItems().stream().noneMatch(s -> s.getIdSala() == datos.salaInactiva);
            }), "La pantalla excluye películas y salas inactivas de sus ComboBox");
            LocalDateTime inicio = datos.inicioFuturo();
            formulario(datos, inicio, "35.50");
            capturar("programacion_funciones.png");
            pulsar("btnGuardar");
            comprobar(mensaje().contains("programada correctamente"), "Guardar crea una función desde el formulario");
            Funcion creada = dao.listarTodos().stream().filter(f -> f.getIdSala() == datos.sala1 && f.getFechaInicio().equals(inicio)).findFirst().orElseThrow();
            seleccionar(creada.getIdFuncion());
            comprobar(fx(() -> ((ComboBox<?>) escenario.getScene().lookup("#cmbPelicula")).getValue() == null
                    && ((Label) escenario.getScene().lookup("#lblModo")).getText().equals("Nueva función")),
                    "Seleccionar una fila no activa ni rellena la edición");
            pulsar("btnEditar");
            comprobar(fx(() -> ((Label) escenario.getScene().lookup("#lblModo")).getText().contains("Editar función")),
                    "Editar carga el formulario y cambia su modo");
            fx(() -> { ((TextField) escenario.getScene().lookup("#txtPrecio")).setText("45.75"); return null; });
            pulsar("btnGuardar");
            comprobar(dao.buscarPorId(creada.getIdFuncion()).orElseThrow().getPrecioBoleto().compareTo(new java.math.BigDecimal("45.75")) == 0,
                    "Guardar cambios persiste la edición en MySQL");
            pulsar("btnNueva");
            formulario(datos, inicio.plusMinutes(5), "35.00");
            pulsar("btnGuardar");
            comprobar(mensaje().contains("cruza"), "La pantalla explica el conflicto de horarios SQL");
            capturar("conflicto_horario.png");
            formulario(datos, inicio.plusDays(1), "0");
            pulsar("btnGuardar");
            comprobar(mensaje().contains("mayor que cero"), "El formulario rechaza un precio cero");
            formulario(datos, inicio.plusDays(1), "35.00");
            fx(() -> { ((TextField) escenario.getScene().lookup("#txtHora")).setText("24:00"); return null; });
            pulsar("btnGuardar");
            comprobar(mensaje().contains("23:59"), "El formulario rechaza una hora fuera de rango");
            formulario(datos, inicio.plusDays(1), "35.00");
            fx(() -> { ((DatePicker) escenario.getScene().lookup("#dpFecha")).getEditor().setText("31/02/2030"); return null; });
            pulsar("btnGuardar");
            comprobar(mensaje().contains("fecha válida"), "El formulario rechaza una fecha inexistente");
            formulario(datos, dao.obtenerHoraServidor().minusDays(1), "35.00");
            pulsar("btnGuardar");
            comprobar(mensaje().contains("fecha futura"), "El formulario compara la fecha con la hora del servidor");
            formulario(datos, inicio.plusDays(1), "35.123");
            pulsar("btnGuardar");
            comprobar(mensaje().contains("dos decimales"), "El formulario evita el redondeo de precios con tres decimales");
            seleccionar(creada.getIdFuncion());
            pulsar("btnCancelar");
            comprobar("cancelada".equals(dao.buscarPorId(creada.getIdFuncion()).orElseThrow().getEstado()),
                    "Cancelar función cambia el estado tras confirmar");
            Funcion futura = new Funcion(datos.pelicula1, datos.sala1, inicio.plusDays(2), new java.math.BigDecimal("35"));
            dao.insertar(futura);
            pulsar("btnActualizar");
            seleccionar(futura.getIdFuncion());
            pulsar("btnFinalizar");
            comprobar(mensaje().contains("puede finalizarse después"), "Finalizar antes del horario muestra una explicación");
            int vencida = datos.funcionVencida();
            pulsar("btnActualizar");
            seleccionar(vencida);
            pulsar("btnFinalizar");
            comprobar("finalizada".equals(dao.buscarPorId(vencida).orElseThrow().getEstado()),
                    "La pantalla finaliza una función cuyo horario terminó");
            capturar("cancelacion_finalizacion.png");
            fx(() -> { ((ComboBox<String>) escenario.getScene().lookup("#cmbFiltroEstado")).setValue("Canceladas"); return null; });
            comprobar(fx(() -> ((TableView<Funcion>) escenario.getScene().lookup("#tblFunciones")).getItems()
                    .stream().allMatch(f -> "cancelada".equals(f.getEstado()))), "El filtro de estado muestra solo funciones canceladas");
            pulsar("btnVolver");
            comprobar(fx(() -> escenario.getScene().lookup("#btnFunciones") != null && SesionContext.haySesionActiva()),
                    "Volver regresa al dashboard y conserva la sesión");
        }
        System.out.println("RESULTADO: " + comprobaciones + " comprobaciones de interfaz y evidencia correctas.");
    }

    public static void main(String[] args) {
        if (!Boolean.getBoolean("cine.pruebas.bd")) throw new IllegalStateException("Usa una base de pruebas y -Dcine.pruebas.bd=true.");
        launch(args);
        if (fallo != null) throw new AssertionError("Falló la prueba de programación", fallo);
    }
}
