package org.cine.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.cine.dao.FuncionDAO;
import org.cine.dao.impl.FuncionDAOImpl;
import org.cine.model.Funcion;
import org.cine.model.Pelicula;
import org.cine.model.Sala;
import org.cine.model.Usuario;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

/** Alta, edición y cambios de estado de funciones de US-2.2. */
public class FuncionController {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final int MINUTOS_LIMPIEZA = 20;

    @FXML private TableView<Funcion> tblFunciones;
    @FXML private TableColumn<Funcion, Number> colId;
    @FXML private TableColumn<Funcion, String> colPelicula;
    @FXML private TableColumn<Funcion, String> colSala;
    @FXML private TableColumn<Funcion, String> colInicio;
    @FXML private TableColumn<Funcion, String> colFin;
    @FXML private TableColumn<Funcion, String> colPrecio;
    @FXML private TableColumn<Funcion, String> colEstado;
    @FXML private ComboBox<Pelicula> cmbPelicula;
    @FXML private ComboBox<Sala> cmbSala;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private Label lblModo;
    @FXML private Label lblFinCalculado;
    @FXML private Label lblSeleccion;
    @FXML private Label lblResumen;
    @FXML private Label lblMensaje;
    @FXML private Button btnGuardar;
    @FXML private Button btnEditar;
    @FXML private Button btnCancelar;
    @FXML private Button btnFinalizar;

    private final FuncionDAO dao = new FuncionDAOImpl();
    private final ObservableList<Funcion> funciones = FXCollections.observableArrayList();
    private final FilteredList<Funcion> filtradas = new FilteredList<>(funciones, funcion -> true);
    private int idEdicion;
    private LocalDateTime horaServidor;
    private boolean datosDisponibles;

    @FXML
    private void initialize() {
        exigirAdministrador();
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getIdFuncion()));
        colPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTituloPelicula()));
        colSala.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreSala()));
        colInicio.setCellValueFactory(d -> new SimpleStringProperty(FECHA_HORA.format(d.getValue().getFechaInicio())));
        colFin.setCellValueFactory(d -> new SimpleStringProperty(FECHA_HORA.format(d.getValue().getFechaFin())));
        colPrecio.setCellValueFactory(d -> new SimpleStringProperty("Q " + d.getValue().getPrecioBoleto().toPlainString()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean vacia) {
                super.updateItem(estado, vacia);
                getStyleClass().removeAll("estado-programada", "estado-cancelada", "estado-finalizada");
                setText(vacia || estado == null ? null : estado.substring(0, 1).toUpperCase(Locale.ROOT) + estado.substring(1));
                if (!vacia && estado != null) getStyleClass().add("estado-" + estado);
            }
        });
        tblFunciones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblFunciones.setPlaceholder(new Label("No hay funciones para los filtros seleccionados."));
        SortedList<Funcion> ordenadas = new SortedList<>(filtradas);
        ordenadas.comparatorProperty().bind(tblFunciones.comparatorProperty());
        tblFunciones.setItems(ordenadas);
        tblFunciones.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> actualizarAcciones());
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "Programadas", "Canceladas", "Finalizadas"));
        cmbFiltroEstado.setValue("Todos");
        cmbFiltroEstado.valueProperty().addListener((obs, anterior, actual) -> filtrar());
        txtBusqueda.textProperty().addListener((obs, anterior, actual) -> filtrar());
        dpFecha.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate fecha) { return fecha == null ? "" : FECHA.format(fecha); }
            @Override public LocalDate fromString(String texto) {
                return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim(), FECHA);
            }
        });
        cmbPelicula.valueProperty().addListener((obs, anterior, actual) -> actualizarFin());
        dpFecha.valueProperty().addListener((obs, anterior, actual) -> actualizarFin());
        dpFecha.getEditor().textProperty().addListener((obs, anterior, actual) -> actualizarFin());
        txtHora.textProperty().addListener((obs, anterior, actual) -> actualizarFin());
        try {
            cargarDatos();
            limpiarFormulario();
            mensaje("Selecciona Nueva función o una fila y después Editar.", false);
        } catch (SQLException ex) {
            btnGuardar.setDisable(true);
            errorSql(ex);
        }
        actualizarAcciones();
    }

    private void exigirAdministrador() {
        Usuario actual = SesionContext.getUsuarioActual();
        if (actual == null || !actual.isEstado() || !"admin".equals(actual.getNombreRol())) {
            throw new IllegalStateException("La programación de funciones requiere una sesión de administrador.");
        }
    }

    private void cargarDatos() throws SQLException {
        List<Funcion> nuevas = dao.listarTodos();
        List<Pelicula> peliculas = dao.listarPeliculasActivas();
        List<Sala> salas = dao.listarSalasActivas();
        LocalDateTime ahora = dao.obtenerHoraServidor();
        Pelicula peliculaAnterior = cmbPelicula.getValue();
        Sala salaAnterior = cmbSala.getValue();
        cmbPelicula.setItems(FXCollections.observableArrayList(peliculas));
        cmbSala.setItems(FXCollections.observableArrayList(salas));
        if (peliculaAnterior != null) seleccionarPelicula(peliculaAnterior.getIdPelicula());
        if (salaAnterior != null) seleccionarSala(salaAnterior.getIdSala());
        funciones.setAll(nuevas);
        horaServidor = ahora;
        datosDisponibles = true;
        btnGuardar.setDisable(peliculas.isEmpty() || salas.isEmpty());
        filtrar();
        actualizarAcciones();
    }

    private void filtrar() {
        String texto = txtBusqueda.getText() == null ? "" : txtBusqueda.getText().trim().toLowerCase(Locale.ROOT);
        String filtro = cmbFiltroEstado.getValue();
        String estado = switch (filtro == null ? "Todos" : filtro) {
            case "Programadas" -> "programada";
            case "Canceladas" -> "cancelada";
            case "Finalizadas" -> "finalizada";
            default -> "";
        };
        filtradas.setPredicate(f -> (estado.isEmpty() || estado.equals(f.getEstado()))
                && (texto.isEmpty() || (f.getTituloPelicula() + " " + f.getNombreSala() + " " + f.getIdFuncion())
                        .toLowerCase(Locale.ROOT).contains(texto)));
        long programadas = funciones.stream().filter(Funcion::isProgramada).count();
        lblResumen.setText(filtradas.size() + " de " + funciones.size() + " funciones · " + programadas + " programadas");
    }

    private void actualizarAcciones() {
        Funcion seleccionada = tblFunciones.getSelectionModel().getSelectedItem();
        boolean programada = seleccionada != null && seleccionada.isProgramada();
        btnEditar.setDisable(!programada);
        btnCancelar.setDisable(!programada || seleccionada.getBoletosActivos() > 0);
        btnFinalizar.setDisable(!programada);
        if (seleccionada == null) {
            lblSeleccion.setText("Selecciona una función para consultar sus acciones.");
        } else {
            String detalle = "Función #" + seleccionada.getIdFuncion() + " · " + seleccionada.getEstado();
            if (seleccionada.getBoletosActivos() > 0) detalle += " · " + seleccionada.getBoletosActivos() + " boletos activos; anula sus ventas antes de cancelar.";
            else if (programada) detalle += " · La finalización se permite cuando termina su horario.";
            lblSeleccion.setText(detalle);
        }
    }

    @FXML
    private void nuevaFuncion() {
        limpiarFormulario();
        mensaje("Completa los datos y pulsa Guardar función.", false);
    }

    private void limpiarFormulario() {
        idEdicion = 0;
        lblModo.setText("Nueva función");
        btnGuardar.setText("Guardar función");
        cmbPelicula.getSelectionModel().clearSelection();
        cmbSala.getSelectionModel().clearSelection();
        LocalDateTime sugerencia = (horaServidor == null ? LocalDateTime.now() : horaServidor).plusHours(1).withSecond(0).withNano(0);
        dpFecha.setValue(sugerencia.toLocalDate());
        txtHora.setText(HORA.format(sugerencia.toLocalTime()));
        txtPrecio.clear();
        actualizarFin();
    }

    @FXML
    private void editarFuncion() {
        try {
            exigirAdministrador();
            Funcion seleccionada = seleccionProgramada();
            Funcion actual = dao.buscarPorId(seleccionada.getIdFuncion())
                    .orElseThrow(() -> new IllegalArgumentException("La función ya no existe. Actualiza la lista."));
            if (!actual.isProgramada()) throw new IllegalArgumentException("Solo se puede editar una función programada.");
            if (!seleccionarPelicula(actual.getIdPelicula()) || !seleccionarSala(actual.getIdSala())) {
                throw new IllegalArgumentException("La película o sala ya no está activa. Actualiza los catálogos antes de editar.");
            }
            idEdicion = actual.getIdFuncion();
            dpFecha.setValue(actual.getFechaInicio().toLocalDate());
            txtHora.setText(HORA.format(actual.getFechaInicio().toLocalTime()));
            txtPrecio.setText(actual.getPrecioBoleto().toPlainString());
            lblModo.setText("Editar función #" + idEdicion);
            btnGuardar.setText("Guardar cambios");
            mensaje("Edita los datos y pulsa Guardar cambios. El historial de boletos puede impedir la edición.", false);
        } catch (SQLException ex) { errorSql(ex); }
        catch (IllegalArgumentException ex) { mensaje(ex.getMessage(), true); }
    }

    @FXML
    private void guardarFuncion() {
        try {
            exigirAdministrador();
            if (!datosDisponibles) throw new IllegalArgumentException("Actualiza la lista antes de guardar.");
            Pelicula pelicula = cmbPelicula.getValue();
            Sala sala = cmbSala.getValue();
            if (pelicula == null || sala == null) throw new IllegalArgumentException("Selecciona una película y una sala activas.");
            LocalDateTime inicio = LocalDateTime.of(leerFecha(), leerHora());
            if (!inicio.isAfter(dao.obtenerHoraServidor())) throw new IllegalArgumentException("La función debe programarse para una fecha futura.");
            String precio = txtPrecio.getText() == null ? "" : txtPrecio.getText().trim();
            if (!precio.matches("[0-9]{1,8}(?:[.,][0-9]{1,2})?")) {
                throw new IllegalArgumentException("Escribe un precio positivo con hasta dos decimales, por ejemplo 35.00.");
            }
            BigDecimal importe = new BigDecimal(precio.replace(',', '.'));
            if (importe.signum() <= 0) throw new IllegalArgumentException("El precio del boleto debe ser mayor que cero.");
            Funcion funcion = new Funcion(pelicula.getIdPelicula(), sala.getIdSala(), inicio, importe);
            int id;
            String resultado;
            if (idEdicion == 0) {
                id = dao.insertar(funcion);
                resultado = "Función #" + id + " programada correctamente.";
            } else {
                id = idEdicion;
                funcion.setIdFuncion(id);
                dao.actualizar(funcion);
                resultado = "Cambios de la función #" + id + " guardados correctamente.";
            }
            cargarDatos();
            limpiarFormulario();
            seleccionarFuncion(id);
            mensaje(resultado, false);
        } catch (SQLException ex) { errorSql(ex); }
        catch (IllegalArgumentException ex) { mensaje(ex.getMessage(), true); }
    }

    @FXML
    private void cancelarFuncion() {
        try {
            exigirAdministrador();
            Funcion seleccionada = seleccionProgramada();
            if (!confirmar("Cancelar función", seleccionada)) return;
            dao.cancelar(seleccionada.getIdFuncion());
            cargarDatos();
            if (idEdicion == seleccionada.getIdFuncion()) limpiarFormulario();
            seleccionarFuncion(seleccionada.getIdFuncion());
            mensaje("Función #" + seleccionada.getIdFuncion() + " cancelada. Se conserva su historial.", false);
        } catch (SQLException ex) { errorSql(ex); }
        catch (IllegalArgumentException ex) { mensaje(ex.getMessage(), true); }
    }

    @FXML
    private void finalizarFuncion() {
        try {
            exigirAdministrador();
            Funcion seleccionada = seleccionProgramada();
            if (seleccionada.getFechaFin().isAfter(dao.obtenerHoraServidor())) {
                throw new IllegalArgumentException("La función puede finalizarse después del " + FECHA_HORA.format(seleccionada.getFechaFin()) + ".");
            }
            if (!confirmar("Finalizar función", seleccionada)) return;
            dao.finalizar(seleccionada.getIdFuncion());
            cargarDatos();
            if (idEdicion == seleccionada.getIdFuncion()) limpiarFormulario();
            seleccionarFuncion(seleccionada.getIdFuncion());
            mensaje("Función #" + seleccionada.getIdFuncion() + " finalizada correctamente.", false);
        } catch (SQLException ex) { errorSql(ex); }
        catch (IllegalArgumentException ex) { mensaje(ex.getMessage(), true); }
    }

    private Funcion seleccionProgramada() {
        Funcion funcion = tblFunciones.getSelectionModel().getSelectedItem();
        if (funcion == null || !funcion.isProgramada()) throw new IllegalArgumentException("Selecciona una función programada.");
        return funcion;
    }

    private boolean confirmar(String accion, Funcion funcion) {
        Alert aviso = new Alert(Alert.AlertType.CONFIRMATION,
                accion + " #" + funcion.getIdFuncion() + " de " + funcion.getNombreSala() + "?", ButtonType.YES, ButtonType.NO);
        aviso.setTitle(accion);
        aviso.setHeaderText(funcion.getTituloPelicula());
        aviso.initOwner(tblFunciones.getScene().getWindow());
        return aviso.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    @FXML
    private void actualizarLista() {
        try {
            cargarDatos();
            mensaje("Lista y catálogos actualizados.", false);
        } catch (SQLException ex) { errorSql(ex); }
    }

    @FXML
    private void volver() throws IOException {
        Principal.mostrarDashboardSegunRol();
    }

    private LocalDate leerFecha() {
        String texto = dpFecha.getEditor().getText();
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException("Selecciona una fecha.");
        try { return LocalDate.parse(texto.trim(), FECHA); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("Escribe una fecha válida con formato dd/MM/aaaa."); }
    }

    private LocalTime leerHora() {
        String texto = txtHora.getText() == null ? "" : txtHora.getText().trim();
        if (!texto.matches("[0-9]{2}:[0-9]{2}")) throw new IllegalArgumentException("Escribe la hora en formato HH:mm, por ejemplo 18:30.");
        try { return LocalTime.parse(texto, HORA); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("La hora debe estar entre 00:00 y 23:59."); }
    }

    private void actualizarFin() {
        if (lblFinCalculado == null || cmbPelicula.getValue() == null) {
            if (lblFinCalculado != null) lblFinCalculado.setText("El final se calcula al elegir película, fecha y hora.");
            return;
        }
        try {
            int duracion = cmbPelicula.getValue().getDuracionMinutos();
            LocalDateTime fin = LocalDateTime.of(leerFecha(), leerHora()).plusMinutes(duracion + MINUTOS_LIMPIEZA);
            lblFinCalculado.setText("Finaliza: " + FECHA_HORA.format(fin) + "\n" + duracion + " min de película + 20 min de limpieza.");
        } catch (IllegalArgumentException ex) {
            lblFinCalculado.setText("Completa una fecha y hora válidas para calcular el final.");
        }
    }

    private boolean seleccionarPelicula(int id) {
        cmbPelicula.setValue(cmbPelicula.getItems().stream().filter(p -> p.getIdPelicula() == id).findFirst().orElse(null));
        return cmbPelicula.getValue() != null;
    }

    private boolean seleccionarSala(int id) {
        cmbSala.setValue(cmbSala.getItems().stream().filter(s -> s.getIdSala() == id).findFirst().orElse(null));
        return cmbSala.getValue() != null;
    }

    private void seleccionarFuncion(int id) {
        for (Funcion funcion : tblFunciones.getItems()) {
            if (funcion.getIdFuncion() == id) {
                tblFunciones.getSelectionModel().select(funcion);
                tblFunciones.scrollTo(funcion);
                break;
            }
        }
    }

    private void errorSql(SQLException ex) {
        String texto;
        String estado = ex.getSQLState() == null ? "" : ex.getSQLState();
        if ("45000".equals(estado)) texto = ex.getMessage();
        else if (ex.getErrorCode() == 1305) texto = "Falta un procedimiento de funciones en la base de datos. Las tareas SQL están pendientes de integración.";
        else if (ex.getErrorCode() == 1045) texto = "MySQL rechazó el usuario o la contraseña. Revisa db.properties.";
        else if (ex.getErrorCode() == 1049) texto = "La base de datos configurada no existe. Revisa db.properties.";
        else if (estado.startsWith("08")) texto = "No se pudo conectar con MySQL. Revisa el servidor y la dirección en db.properties y pulsa Actualizar.";
        else if (ex.getErrorCode() == 1146 || ex.getErrorCode() == 1054) texto = "La estructura de la base no coincide con el módulo de funciones. Revisa la integración SQL pendiente.";
        else if (estado.startsWith("23")) texto = "No se pudo guardar por una restricción de la base. Actualiza la lista y revisa la película y la sala.";
        else if ("40001".equals(estado) || ex.getErrorCode() == 1205) texto = "Otra operación está usando estos datos. Actualiza la lista e inténtalo nuevamente.";
        else texto = "No se pudo completar la operación en MySQL. Actualiza la lista e inténtalo nuevamente. Código: " + ex.getErrorCode() + ".";
        mensaje(texto, true);
    }

    private void mensaje(String texto, boolean error) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("status-ok", "status-error");
        lblMensaje.getStyleClass().add(error ? "status-error" : "status-ok");
    }
}
