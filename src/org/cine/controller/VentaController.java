package org.cine.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.cine.dao.VentaDAO;
import org.cine.dao.impl.VentaDAOImpl;
import org.cine.model.Boleto;
import org.cine.service.SesionContext;
import org.cine.system.Principal;

public class VentaController {

    private final VentaDAO dao = new VentaDAOImpl();

    @FXML
    private ComboBox<VentaDAO.Opcion> cbCliente;

    @FXML
    private ComboBox<VentaDAO.Funcion> cbFuncion;

    @FXML
    private ComboBox<String> cbMetodoPago;

    @FXML
    private FlowPane panelButacas;

    @FXML
    private Button btnNuevoCliente;

    @FXML
    private ListView<Boleto> listaBoletos;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblEstado;

    private int ventaId = 0;

    private final Set<Integer> seleccion
            = new LinkedHashSet<>();

    // =========================================================
    // INICIALIZACIÓN
    // =========================================================
    @FXML
    private void initialize() {

        try {

            // Cargar clientes
            cbCliente.getItems().setAll(
                    dao.clientes()
            );

            // Cargar funciones
            cbFuncion.getItems().setAll(
                    dao.funciones()
            );

            // Métodos de pago
            cbMetodoPago.getItems().setAll(
                    "EFECTIVO",
                    "TARJETA",
                    "TRANSFERENCIA"
            );

            // Método de pago predeterminado
            cbMetodoPago.getSelectionModel()
                    .select("EFECTIVO");

        } catch (SQLException e) {

            error(e);
        }

        // Cuando cambia la función se cargan sus butacas
        cbFuncion.valueProperty()
                .addListener((observable, anterior, nueva) -> {

                    cargarButacas();
                });

        actualizarTotal();
    }

    // =========================================================
    // CARGAR BUTACAS
    // =========================================================
    private void cargarButacas() {

        seleccion.clear();

        panelButacas.getChildren().clear();

        if (cbFuncion.getValue() == null) {
            return;
        }

        try {

            List<VentaDAO.Butaca> butacas
                    = dao.butacas(
                            cbFuncion.getValue().id()
                    );

            for (VentaDAO.Butaca b : butacas) {

                String texto
                        = b.nombre()
                        + (b.disponible()
                        ? ""
                        : " ✕");

                ToggleButton boton
                        = new ToggleButton(texto);

                boton.setPrefSize(
                        85,
                        45
                );

                boton.setDisable(
                        !b.disponible()
                );

                boton.selectedProperty()
                        .addListener(
                                (observable,
                                        anterior,
                                        seleccionado) -> {

                                    if (seleccionado) {

                                        seleccion.add(
                                                b.id()
                                        );

                                    } else {

                                        seleccion.remove(
                                                b.id()
                                        );
                                    }
                                }
                        );

                panelButacas.getChildren()
                        .add(boton);
            }

        } catch (SQLException e) {

            error(e);
        }
    }

    // =========================================================
    // AGREGAR BUTACAS
    // =========================================================
    @FXML
    private void agregar() {

        if (cbCliente.getValue() == null) {

            aviso(
                    "Debes seleccionar un cliente."
            );

            return;
        }

        if (cbFuncion.getValue() == null) {

            aviso(
                    "Debes seleccionar una función."
            );

            return;
        }

        if (seleccion.isEmpty()) {

            aviso(
                    "Debes seleccionar al menos una butaca disponible."
            );

            return;
        }

        try {

            // Crear venta abierta si todavía no existe
            if (ventaId == 0) {

                if (SesionContext.getUsuarioActual() == null) {

                    aviso(
                            "No existe una sesión de usuario activa."
                    );

                    return;
                }

                ventaId = dao.abrir(
                        cbCliente.getValue().id(),
                        SesionContext
                                .getUsuarioActual()
                                .getIdUsuario()
                );
            }

            // Agregar cada butaca
            for (int idButaca
                    : new ArrayList<>(seleccion)) {

                dao.agregar(
                        ventaId,
                        cbFuncion.getValue().id(),
                        idButaca
                );
            }

            refrescar();

            cargarButacas();

            lblEstado.setText(
                    "Carrito #" + ventaId
            );

        } catch (SQLException e) {

            try {

                refrescar();

            } catch (SQLException ignored) {
            }

            cargarButacas();

            error(e);
        }
    }

    // =========================================================
    // REFRESCAR CARRITO
    // =========================================================
    private void refrescar()
            throws SQLException {

        if (ventaId == 0) {

            listaBoletos.getItems()
                    .clear();

        } else {

            listaBoletos.getItems()
                    .setAll(
                            dao.boletos(ventaId)
                    );
        }

        actualizarTotal();
    }

    // =========================================================
    // ACTUALIZAR TOTAL
    // =========================================================
    private void actualizarTotal() {

        BigDecimal total
                = BigDecimal.ZERO;

        for (Boleto boleto
                : listaBoletos.getItems()) {

            if (boleto != null
                    && boleto.precio() != null) {

                total = total.add(
                        boleto.precio()
                );
            }
        }

        lblTotal.setText(
                "Total: Q"
                + total
        );
    }

    // =========================================================
    // QUITAR BOLETO
    // =========================================================
    @FXML
    private void quitar() {

        Boleto boleto
                = listaBoletos
                        .getSelectionModel()
                        .getSelectedItem();

        if (boleto == null) {

            aviso(
                    "Selecciona un boleto del carrito."
            );

            return;
        }

        if (ventaId == 0) {
            return;
        }

        try {

            dao.quitar(
                    ventaId,
                    boleto.id()
            );

            refrescar();

            cargarButacas();

        } catch (SQLException e) {

            error(e);
        }
    }

    // =========================================================
    // CONFIRMAR VENTA
    // =========================================================
    @FXML
    private void confirmar() {

        if (cbCliente.getValue() == null) {

            aviso(
                    "Debes seleccionar un cliente."
            );

            return;
        }

        if (cbFuncion.getValue() == null) {

            aviso(
                    "Debes seleccionar una función."
            );

            return;
        }

        if (ventaId == 0) {

            aviso(
                    "No existe una venta abierta."
            );

            return;
        }

        if (listaBoletos.getItems().isEmpty()) {

            aviso(
                    "Debes agregar al menos un boleto."
            );

            return;
        }

        if (cbMetodoPago.getValue() == null
                || cbMetodoPago.getValue().isBlank()) {

            aviso(
                    "Debes seleccionar un método de pago."
            );

            return;
        }

        try {

            int id = dao.confirmar(
                    ventaId,
                    cbMetodoPago.getValue()
            ).id();

            ventaId = 0;

            seleccion.clear();

            Principal.mostrarFactura(id);

        } catch (SQLException | IOException e) {

            error(e);
        }
    }
    
    // =========================================================
    // CANCELAR VENTA
    // =========================================================

    @FXML
    private void cancelar() {

        if (ventaId == 0) {

            return;
        }

        try {

            dao.cancelar(
                    ventaId
            );

            ventaId = 0;

            seleccion.clear();

            listaBoletos.getItems()
                    .clear();

            actualizarTotal();

            cargarButacas();

            lblEstado.setText(
                    "Carrito cancelado"
            );

        } catch (SQLException e) {

            error(e);
        }
    }

    // =========================================================
    // VOLVER
    // =========================================================
    @FXML
    private void volver()
            throws IOException {

        Principal.mostrarDashboardSegunRol();
    }

    // =========================================================
    // REIMPRIMIR FACTURA
    // =========================================================
    @FXML
    private void reimprimir()
            throws IOException {

        if (ventaId <= 0) {

            aviso(
                    "No existe una venta confirmada para reimprimir."
            );

            return;
        }

        Principal.mostrarFactura(
                ventaId
        );
    }

    // =========================================================
    // RECIBIR FUNCIÓN DESDE CARTELERA
    // =========================================================
    public void seleccionarFuncion(
            int idFuncion) {

        if (cbFuncion == null) {

            throw new IllegalStateException(
                    "El selector de funciones no está inicializado."
            );
        }

        for (VentaDAO.Funcion funcion
                : cbFuncion.getItems()) {

            if (funcion.id() == idFuncion) {

                cbFuncion.setValue(
                        funcion
                );

                System.out.println(
                        "Función recibida desde cartelera: "
                        + idFuncion
                );

                return;
            }
        }

        throw new IllegalArgumentException(
                "La función seleccionada no está disponible para venta."
        );
    }

    // =========================================================
    // NUEVO CLIENTE
    // =========================================================
    @FXML
    private void abrirNuevoCliente() {

        try {

            FXMLLoader loader
                    = new FXMLLoader(
                            getClass().getResource(
                                    "/org/cine/view/NuevoCliente.fxml"
                            )
                    );

            Parent root
                    = loader.load();

            NuevoClienteController controller
                    = loader.getController();

            Stage stage
                    = new Stage();

            stage.initOwner(
                    btnNuevoCliente
                            .getScene()
                            .getWindow()
            );

            stage.initModality(
                    Modality.WINDOW_MODAL
            );

            stage.setTitle(
                    "CINEMA - Nuevo cliente"
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.setResizable(
                    false
            );

            stage.showAndWait();

            // Obtener cliente creado
            VentaDAO.Opcion cliente
                    = controller.getClienteCreado();

            if (cliente == null) {

                return;
            }

            boolean existe = false;

            for (VentaDAO.Opcion opcion
                    : cbCliente.getItems()) {

                if (opcion.id()
                        == cliente.id()) {

                    existe = true;

                    break;
                }
            }

            if (!existe) {

                cbCliente.getItems()
                        .add(cliente);
            }

            // Seleccionar automáticamente
            // el nuevo cliente
            cbCliente.setValue(
                    cliente
            );

        } catch (IOException e) {

            error(e);
        }
    }

    // =========================================================
    // MENSAJE DE ADVERTENCIA
    // =========================================================
    private void aviso(String mensaje) {

        Alert alerta
                = new Alert(
                        Alert.AlertType.WARNING
                );

        alerta.setTitle(
                "CINEMA"
        );

        alerta.setHeaderText(
                null
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }

    // =========================================================
    // MENSAJE DE ERROR
    // =========================================================
    private void error(Exception e) {

        String mensaje
                = e.getMessage();

        if (mensaje == null
                || mensaje.isBlank()) {

            mensaje
                    = "Ocurrió un error inesperado.";
        }

        Alert alerta
                = new Alert(
                        Alert.AlertType.ERROR
                );

        alerta.setTitle(
                "Error"
        );

        alerta.setHeaderText(
                "No se pudo completar la operación"
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }
}
