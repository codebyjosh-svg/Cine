package org.cine.controller;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import org.cine.dao.ButacaDAO;
import org.cine.dao.SalaDAO;
import org.cine.dao.impl.ButacaDAOImpl;
import org.cine.dao.impl.SalaDAOImpl;
import org.cine.model.Butaca;
import org.cine.model.Sala;
import org.cine.system.Principal;

public class SalasController {

    @FXML private ComboBox<String> cboFormato;
    @FXML private Label lblSalaSeleccionada;

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

    @FXML
    private void initialize() {
        configurarTablas();
        cboFormato.setItems(FXCollections.observableArrayList("2D", "3D"));

        tblSalas.getSelectionModel().selectedItemProperty()
                .addListener((observable, anterior, actual) -> {
                    if (actual == null) {
                        limpiarSeleccionSala();
                    } else {
                        mostrarButacas(actual);
                    }
                });

        cargarSalas();
    }

    private void configurarTablas() {
        colIdSala.setCellValueFactory(new PropertyValueFactory<>("idSala"));
        colNombreSala.setCellValueFactory(new PropertyValueFactory<>("nombreSala"));
        colFormato.setCellValueFactory(new PropertyValueFactory<>("formato"));
        colEstadoSala.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getEstado() == 1 ? "Activa" : "Inactiva"
                )
        );

        colIdButaca.setCellValueFactory(new PropertyValueFactory<>("idButaca"));
        colFila.setCellValueFactory(new PropertyValueFactory<>("fila"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colEstadoButaca.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getEstado() == 1 ? "Activa" : "Inactiva"
                )
        );
    }

    private void cargarSalas() {
        List<Sala> salas = salaDAO.listarTodos();

        if (!salaDAO.getUltimoError().isEmpty()) {
            mostrarError("No se pudieron cargar las salas.",
                    salaDAO.getUltimoError());
            return;
        }

        tblSalas.setItems(FXCollections.observableArrayList(salas));
        limpiarSeleccionSala();
    }

    private void mostrarButacas(Sala sala) {
        lblSalaSeleccionada.setText("Butacas de " + sala.getNombreSala());

        List<Butaca> butacas = butacaDAO.listarPorSala(sala.getIdSala());

        if (!butacaDAO.getUltimoError().isEmpty()) {
            mostrarError("No se pudieron cargar las butacas.",
                    butacaDAO.getUltimoError());
            tblButacas.setItems(FXCollections.observableArrayList());
            return;
        }

        tblButacas.setItems(FXCollections.observableArrayList(butacas));
    }

    private void limpiarSeleccionSala() {
        lblSalaSeleccionada.setText(
                "Selecciona una sala para consultar sus butacas."
        );
        tblButacas.setItems(FXCollections.observableArrayList());
    }

    @FXML
    private void regresarDashboard() throws IOException {
        Principal.mostrarDashboardSegunRol();
    }

    private void mostrarError(String encabezado, String detalle) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Gestión de salas y butacas");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(detalle);
        alerta.showAndWait();
    }
}