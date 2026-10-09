package org.cine.controller;

import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.cine.dao.VentaDAO;
import org.cine.dao.impl.VentaDAOImpl;
import org.cine.system.Principal;

public class FacturaController {

    private final VentaDAO dao = new VentaDAOImpl();

    @FXML
    private TextField txtIdVenta;

    @FXML
    private TextArea txtFactura;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblCliente;

    @FXML
    private Label lblEstado;

    @FXML
    private Label lblNumeroFactura;

    @FXML
    private Button btnImprimir;

    /**
     * Muestra la factura recibida desde la venta.
     *
     * @param id identificador de la venta
     */
    public void mostrar(int id) {

        if (id > 0) {

            txtIdVenta.setText(String.valueOf(id));

            consultar();
        }
    }

    /**
     * Consulta la información de la venta.
     */
    @FXML
    private void consultar() {

        try {

            String textoId = txtIdVenta.getText();

            if (textoId == null || textoId.isBlank()) {

                mostrarAviso(
                        Alert.AlertType.WARNING,
                        "Consulta de factura",
                        "Ingresa el número de factura."
                );

                return;
            }

            int id = Integer.parseInt(
                    textoId.trim()
            );

            var venta = dao.consultar(id);

            if (venta == null) {

                throw new SQLException(
                        "No se encontró la venta indicada."
                );
            }

            if (!"confirmada".equalsIgnoreCase(venta.estado())
                    && !"anulada".equalsIgnoreCase(venta.estado())) {

                throw new SQLException(
                        "La venta no está confirmada."
                );
            }

            // ==============================
            // DATOS DEL ENCABEZADO
            // ==============================

            lblNumeroFactura.setText(
                    "#" + id
            );

            lblCliente.setText(
                    venta.cliente()
            );

            lblEstado.setText(
                    venta.estado().toUpperCase()
            );

            lblTotal.setText(
                    "Q" + venta.total()
            );

            // ==============================
            // DETALLE
            // ==============================

            StringBuilder factura =
                    new StringBuilder();

            factura.append(
                    "DETALLE DE LA TRANSACCIÓN\n"
            );

            factura.append(
                    "────────────────────────────────────────────\n\n"
            );

            for (String linea : dao.factura(id)) {

                factura.append(linea)
                       .append("\n");
            }

            txtFactura.setText(
                    factura.toString()
            );

        } catch (SQLException | NumberFormatException e) {

            mostrarAviso(
                    Alert.AlertType.ERROR,
                    "No se pudo consultar la factura",
                    e.getMessage()
            );
        }
    }

    /**
     * Simula el proceso de impresión.
     *
     * No abre el diálogo de impresión del sistema.
     */
    @FXML
    private void imprimir() {

        if (txtFactura.getText() == null
                || txtFactura.getText().isBlank()) {

            mostrarAviso(
                    Alert.AlertType.WARNING,
                    "Imprimir factura",
                    "Primero debes consultar una factura."
            );

            return;
        }

        mostrarAviso(
                Alert.AlertType.INFORMATION,
                "Impresión correcta",
                "La factura fue procesada correctamente."
        );
    }

    /**
     * Prepara la pantalla para consultar otra factura.
     */
    @FXML
    private void nuevaFactura() {

        txtIdVenta.clear();

        txtFactura.clear();

        lblNumeroFactura.setText(
                "—"
        );

        lblCliente.setText(
                "—"
        );

        lblEstado.setText(
                "SIN CONSULTAR"
        );

        lblTotal.setText(
                "Q0.00"
        );

        txtIdVenta.requestFocus();
    }

    /**
     * Reutiliza el proceso de impresión.
     */
    @FXML
    private void reimprimir() {

        imprimir();
    }

    /**
     * Regresa a la pantalla de venta.
     */
    @FXML
    private void volver() throws IOException {

        Principal.mostrarVenta();
    }

    /**
     * Muestra mensajes al usuario.
     */
    private void mostrarAviso(
            Alert.AlertType tipo,
            String titulo,
            String mensaje) {

        Alert alerta =
                new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}