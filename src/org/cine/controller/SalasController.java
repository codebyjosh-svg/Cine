package org.cine.controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import org.cine.dao.ButacaDAO;
import org.cine.dao.SalaDAO;
import org.cine.dao.impl.ButacaDAOImpl;
import org.cine.dao.impl.SalaDAOImpl;
import org.cine.model.Butaca;
import org.cine.model.Sala;
import org.cine.system.Principal;

public class SalasController {

    @FXML private TextField txtNombreSala;
    @FXML private ComboBox<String> cboFormato;
    @FXML private Label lblSalaSeleccionada;
    @FXML private Label lblEstadoSala;
    @FXML private Label lblEstadoButaca;
    @FXML private TextField txtFila;
    @FXML private TextField txtNumeroButaca;

    @FXML private Button btnGuardarSala;
    @FXML private Button btnEditarSala;
    @FXML private Button btnEliminarSala;
    @FXML private Button btnCambiarEstadoSala;
    @FXML private Button btnGuardarButaca;
    @FXML private Button btnEditarButaca;
    @FXML private Button btnEliminarButaca;
    @FXML private Button btnCambiarEstadoButaca;

    @FXML private TableView<Sala> tblSalas;
    @FXML private TableColumn<Sala, Integer> colIdSala;
    @FXML private TableColumn<Sala, String> colNombreSala;
    @FXML private TableColumn<Sala, String> colFormato;
    @FXML private TableColumn<Sala, String> colEstadoSala;

    @FXML private TableView<Butaca> tblButacas;
    @FXML private TableColumn<Butaca, Integer> colIdButaca;
    @FXML private TableColumn<Butaca, String> colFila;
    @FXML private TableColumn<Butaca, Integer> colNumero;
    @FXML private TableColumn<Butaca, String> colEstadoButaca;

    private final SalaDAO salaDAO = new SalaDAOImpl();
    private final ButacaDAO butacaDAO = new ButacaDAOImpl();

    private Sala salaSeleccionada;
    private Butaca butacaSeleccionada;

    @FXML
    private void initialize() {
        configurarTablas();
        cboFormato.setItems(
                FXCollections.observableArrayList("2D", "3D")
        );

        tblSalas.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, actual) -> {
                    if (actual == null) {
                        limpiarSeleccionSala();
                    } else {
                        seleccionarSala(actual);
                    }
                });

        tblButacas.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, actual) -> {
                    if (actual == null) {
                        limpiarSeleccionButaca();
                    } else {
                        seleccionarButaca(actual);
                    }
                });

        cargarSalas();
    }

    private void configurarTablas() {
        colIdSala.setCellValueFactory(
                new PropertyValueFactory<>("idSala")
        );
        colNombreSala.setCellValueFactory(
                new PropertyValueFactory<>("nombreSala")
        );
        colFormato.setCellValueFactory(
                new PropertyValueFactory<>("formato")
        );
        colEstadoSala.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getEstado() == 1
                                ? "Activa"
                                : "Inactiva"
                )
        );

        colIdButaca.setCellValueFactory(
                new PropertyValueFactory<>("idButaca")
        );
        colFila.setCellValueFactory(
                new PropertyValueFactory<>("fila")
        );
        colNumero.setCellValueFactory(
                new PropertyValueFactory<>("numero")
        );
        colEstadoButaca.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getEstado() == 1
                                ? "Activa"
                                : "Inactiva"
                )
        );
    }

    private void cargarSalas() {
        List<Sala> salas = salaDAO.listarTodos();

        if (!salaDAO.getUltimoError().isEmpty()) {
            mostrarError(
                    "No se pudieron cargar las salas.",
                    salaDAO.getUltimoError()
            );
            return;
        }

        tblSalas.setItems(
                FXCollections.observableArrayList(salas)
        );
        tblSalas.getSelectionModel().clearSelection();
        limpiarSeleccionSala();
    }

    private void seleccionarSala(Sala sala) {
        salaSeleccionada = sala;
        txtNombreSala.setText(sala.getNombreSala());
        cboFormato.setValue(sala.getFormato());

        lblEstadoSala.setText(
                sala.getEstado() == 1 ? "Activa" : "Inactiva"
        );
        btnCambiarEstadoSala.setText(
                sala.getEstado() == 1 ? "Desactivar sala" : "Activar sala"
        );

        btnGuardarSala.setDisable(true);
        btnEditarSala.setDisable(false);
        btnEliminarSala.setDisable(false);
        btnCambiarEstadoSala.setDisable(false);

        mostrarButacas(sala);
    }

    private void mostrarButacas(Sala sala) {
        lblSalaSeleccionada.setText(
                "Butacas de " + sala.getNombreSala()
        );

        tblButacas.getSelectionModel().clearSelection();

        List<Butaca> butacas =
                butacaDAO.listarPorSala(sala.getIdSala());

        if (!butacaDAO.getUltimoError().isEmpty()) {
            mostrarError(
                    "No se pudieron cargar las butacas.",
                    butacaDAO.getUltimoError()
            );
            tblButacas.setItems(FXCollections.observableArrayList());
            return;
        }

        tblButacas.setItems(
                FXCollections.observableArrayList(butacas)
        );
        limpiarSeleccionButaca();
    }

    private void limpiarSeleccionSala() {
        salaSeleccionada = null;
        txtNombreSala.clear();
        cboFormato.getSelectionModel().clearSelection();

        lblEstadoSala.setText("Nueva sala - se guardará activa");
        btnGuardarSala.setDisable(false);
        btnEditarSala.setDisable(true);
        btnEliminarSala.setDisable(true);
        btnCambiarEstadoSala.setDisable(true);

        lblSalaSeleccionada.setText(
                "Selecciona una sala para consultar sus butacas."
        );
        tblButacas.setItems(FXCollections.observableArrayList());
        limpiarSeleccionButaca();
    }

    private void seleccionarButaca(Butaca butaca) {
        butacaSeleccionada = butaca;
        txtFila.setText(butaca.getFila());
        txtNumeroButaca.setText(
                String.valueOf(butaca.getNumero())
        );

        lblEstadoButaca.setText(
                butaca.getEstado() == 1 ? "Activa" : "Inactiva"
        );
        btnCambiarEstadoButaca.setText(
                butaca.getEstado() == 1
                        ? "Desactivar butaca"
                        : "Activar butaca"
        );

        btnGuardarButaca.setDisable(true);
        btnEditarButaca.setDisable(false);
        btnEliminarButaca.setDisable(false);
        btnCambiarEstadoButaca.setDisable(false);
    }

    private void limpiarSeleccionButaca() {
        butacaSeleccionada = null;
        txtFila.clear();
        txtNumeroButaca.clear();

        lblEstadoButaca.setText("Nueva butaca - se guardará activa");
        btnGuardarButaca.setDisable(salaSeleccionada == null);
        btnEditarButaca.setDisable(true);
        btnEliminarButaca.setDisable(true);
        btnCambiarEstadoButaca.setDisable(true);
    }

    @FXML
    private void guardarSala() {
        if (salaSeleccionada != null) {
            mostrarAdvertencia(
                    "Pulsa Limpiar para registrar una nueva sala."
            );
            return;
        }

        if (!validarFormularioSala()) {
            return;
        }

        Sala sala = new Sala(
                0,
                txtNombreSala.getText().trim(),
                cboFormato.getValue(),
                1
        );

        if (salaDAO.insertar(sala)) {
            mostrarInformacion("La sala se guardó correctamente.");
            cargarSalas();
        } else {
            mostrarError(
                    "No se pudo guardar la sala.",
                    salaDAO.getUltimoError()
            );
        }
    }

    @FXML
    private void editarSala() {
        if (salaSeleccionada == null) {
            mostrarAdvertencia("Selecciona una sala para editar.");
            return;
        }

        if (!validarFormularioSala()) {
            return;
        }

        salaSeleccionada.setNombreSala(
                txtNombreSala.getText().trim()
        );
        salaSeleccionada.setFormato(cboFormato.getValue());

        if (salaDAO.actualizar(salaSeleccionada)) {
            mostrarInformacion("La sala se actualizó correctamente.");
            cargarSalas();
        } else {
            mostrarError(
                    "No se pudo actualizar la sala.",
                    salaDAO.getUltimoError()
            );
        }
    }

    @FXML
    private void eliminarSala() {
        if (salaSeleccionada == null) {
            mostrarAdvertencia("Selecciona una sala para eliminar.");
            return;
        }

        if (!confirmarOperacion(
                "Eliminar sala",
                "¿Deseas eliminar la sala "
                        + salaSeleccionada.getNombreSala() + "?"
        )) {
            return;
        }

        if (salaDAO.eliminar(salaSeleccionada.getIdSala())) {
            mostrarInformacion("La sala se eliminó correctamente.");
            cargarSalas();
        } else {
            mostrarError(
                    "No se pudo eliminar la sala.",
                    salaDAO.getUltimoError()
            );
        }
    }

    @FXML
    private void cambiarEstadoSala() {
        if (salaSeleccionada == null) {
            mostrarAdvertencia(
                    "Selecciona una sala para cambiar su estado."
            );
            return;
        }

        int nuevoEstado =
                salaSeleccionada.getEstado() == 1 ? 0 : 1;

        if (salaDAO.cambiarEstado(
                salaSeleccionada.getIdSala(),
                nuevoEstado
        )) {
            mostrarInformacion("El estado de la sala se actualizó.");
            cargarSalas();
        } else {
            mostrarError(
                    "No se pudo cambiar el estado de la sala.",
                    salaDAO.getUltimoError()
            );
        }
    }

    @FXML
    private void limpiarFormularioSala() {
        tblSalas.getSelectionModel().clearSelection();
        limpiarSeleccionSala();
    }

    @FXML
    private void guardarButaca() {
        if (salaSeleccionada == null) {
            mostrarAdvertencia(
                    "Selecciona una sala para agregar butacas."
            );
            return;
        }

        if (!validarFilaButaca()) {
            return;
        }

        Integer numero = obtenerNumeroButaca();
        if (numero == null) {
            return;
        }

        if (existeButacaDuplicada(
                txtFila.getText(),
                numero,
                0
        )) {
            mostrarAdvertencia(
                    "Ya existe una butaca con esa fila y número "
                            + "en la sala seleccionada."
            );
            txtFila.requestFocus();
            return;
        }

        Butaca butaca = new Butaca(
                0,
                salaSeleccionada.getIdSala(),
                txtFila.getText().trim(),
                numero,
                1
        );

        if (butacaDAO.insertar(butaca)) {
            mostrarInformacion("La butaca se agregó correctamente.");
            limpiarFormularioButaca();
            mostrarButacas(salaSeleccionada);
        } else {
            mostrarError(
                    "No se pudo agregar la butaca.",
                    butacaDAO.getUltimoError()
            );
        }
    }

    @FXML
    private void editarButaca() {
        if (butacaSeleccionada == null || salaSeleccionada == null) {
            mostrarAdvertencia("Selecciona una butaca para editar.");
            return;
        }

        if (!validarFilaButaca()) {
            return;
        }

        Integer numero = obtenerNumeroButaca();
        if (numero == null) {
            return;
        }

        if (existeButacaDuplicada(
                txtFila.getText(),
                numero,
                butacaSeleccionada.getIdButaca()
        )) {
            mostrarAdvertencia(
                    "Ya existe una butaca con esa fila y número "
                            + "en la sala seleccionada."
            );
            txtFila.requestFocus();
            return;
        }

        butacaSeleccionada.setIdSala(salaSeleccionada.getIdSala());
        butacaSeleccionada.setFila(txtFila.getText().trim());
        butacaSeleccionada.setNumero(numero);

        if (butacaDAO.actualizar(butacaSeleccionada)) {
            mostrarInformacion("La butaca se actualizó correctamente.");
            mostrarButacas(salaSeleccionada);
        } else {
            mostrarError(
                    "No se pudo actualizar la butaca.",
                    butacaDAO.getUltimoError()
            );
        }
    }

    @FXML
    private void eliminarButaca() {
        if (butacaSeleccionada == null) {
            mostrarAdvertencia("Selecciona una butaca para eliminar.");
            return;
        }

        if (!confirmarOperacion(
                "Eliminar butaca",
                "¿Deseas eliminar la butaca "
                        + butacaSeleccionada.getUbicacion() + "?"
        )) {
            return;
        }

        if (butacaDAO.eliminar(butacaSeleccionada.getIdButaca())) {
            mostrarInformacion("La butaca se eliminó correctamente.");
            limpiarFormularioButaca();
            mostrarButacas(salaSeleccionada);
        } else {
            mostrarError(
                    "No se pudo eliminar la butaca.",
                    butacaDAO.getUltimoError()
            );
        }
    }

    @FXML
    private void cambiarEstadoButaca() {
        if (butacaSeleccionada == null) {
            mostrarAdvertencia(
                    "Selecciona una butaca para cambiar su estado."
            );
            return;
        }

        int nuevoEstado =
                butacaSeleccionada.getEstado() == 1 ? 0 : 1;

        if (butacaDAO.cambiarEstado(
                butacaSeleccionada.getIdButaca(),
                nuevoEstado
        )) {
            mostrarInformacion("El estado de la butaca se actualizó.");
            mostrarButacas(salaSeleccionada);
        } else {
            mostrarError(
                    "No se pudo cambiar el estado de la butaca.",
                    butacaDAO.getUltimoError()
            );
        }
    }

    @FXML
    private void limpiarFormularioButaca() {
        tblButacas.getSelectionModel().clearSelection();
        limpiarSeleccionButaca();
    }

    private Integer obtenerNumeroButaca() {
        try {
            int numero = Integer.parseInt(
                    txtNumeroButaca.getText().trim()
            );

            if (numero <= 0) {
                mostrarAdvertencia(
                        "El número de butaca debe ser mayor que 0."
                );
                txtNumeroButaca.requestFocus();
                return null;
            }

            return numero;
        } catch (NumberFormatException e) {
            mostrarAdvertencia(
                    "El número de butaca debe ser un entero."
            );
            txtNumeroButaca.requestFocus();
            return null;
        }
    }

    private boolean validarFormularioSala() {
        String nombre = txtNombreSala.getText() == null
                ? ""
                : txtNombreSala.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAdvertencia("Debes ingresar el nombre de la sala.");
            txtNombreSala.requestFocus();
            return false;
        }

        if (nombre.length() > 100) {
            mostrarAdvertencia(
                    "El nombre de la sala no puede superar "
                            + "los 100 caracteres."
            );
            txtNombreSala.requestFocus();
            txtNombreSala.selectAll();
            return false;
        }

        String formato = cboFormato.getValue();
        if (!"2D".equals(formato) && !"3D".equals(formato)) {
            mostrarAdvertencia("Selecciona el formato 2D o 3D.");
            cboFormato.requestFocus();
            return false;
        }

        for (Sala existente : tblSalas.getItems()) {
            boolean esOtraSala = salaSeleccionada == null
                    || existente.getIdSala()
                            != salaSeleccionada.getIdSala();

            if (esOtraSala
                    && existente.getNombreSala() != null
                    && existente.getNombreSala().trim()
                            .equalsIgnoreCase(nombre)) {
                mostrarAdvertencia("Ya existe una sala con ese nombre.");
                txtNombreSala.requestFocus();
                txtNombreSala.selectAll();
                return false;
            }
        }

        return true;
    }

    private boolean validarFilaButaca() {
        String fila = txtFila.getText() == null
                ? ""
                : txtFila.getText().trim();

        if (fila.isEmpty()) {
            mostrarAdvertencia("Debes ingresar la fila de la butaca.");
            txtFila.requestFocus();
            return false;
        }

        if (fila.length() > 3) {
            mostrarAdvertencia(
                    "La fila no puede superar 3 caracteres."
            );
            txtFila.requestFocus();
            txtFila.selectAll();
            return false;
        }

        txtFila.setText(fila.toUpperCase(Locale.ROOT));
        return true;
    }

    private boolean existeButacaDuplicada(
            String fila,
            int numero,
            int idButacaExcluir) {

        String filaNormalizada =
                fila.trim().toUpperCase(Locale.ROOT);

        for (Butaca existente : tblButacas.getItems()) {
            boolean esOtraButaca =
                    existente.getIdButaca() != idButacaExcluir;

            boolean mismaUbicacion =
                    existente.getFila() != null
                    && existente.getFila().trim()
                            .equalsIgnoreCase(filaNormalizada)
                    && existente.getNumero() == numero;

            if (esOtraButaca && mismaUbicacion) {
                return true;
            }
        }

        return false;
    }

    @FXML
    private void regresarDashboard() throws IOException {
        Principal.mostrarDashboardSegunRol();
    }

    private boolean confirmarOperacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        return alerta.showAndWait()
                .orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void mostrarAdvertencia(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Gestión de salas y butacas");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarInformacion(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Gestión de salas y butacas");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String encabezado, String detalle) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Gestión de salas y butacas");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(detalle);
        alerta.showAndWait();
    }
}