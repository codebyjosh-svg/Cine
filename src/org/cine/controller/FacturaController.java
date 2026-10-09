package org.cine.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.cine.dao.VentaDAO;
import org.cine.dao.impl.VentaDAOImpl;
import org.cine.model.FacturaVenta;
import org.cine.model.LineaFactura;
import org.cine.model.VentaResumen;
import org.cine.service.PermisosVenta;
import org.cine.system.Principal;
import org.cine.util.Formato;
import org.cine.util.TareasFX;

public final class FacturaController {

    @FXML
    private Label lblNumero;

    @FXML
    private Label lblFecha;

    @FXML
    private Label lblCliente;

    @FXML
    private Label lblTaquillero;

    @FXML
    private Label lblMensaje;

    @FXML
    private Label lblSubtotalProductos;

    @FXML
    private Label lblSubtotalBoletos;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblEstado;

    @FXML
    private Label lblNumeroFactura;

    @FXML
    private TableView<LineaFactura> tblFactura;

    @FXML
    private TableColumn<LineaFactura, String> colTipo;

    @FXML
    private TableColumn<LineaFactura, String> colDescripcion;

    @FXML
    private TableColumn<LineaFactura, Integer> colCantidad;

    @FXML
    private TableColumn<LineaFactura, String> colPrecio;

    @FXML
    private TableColumn<LineaFactura, String> colSubtotal;

    @FXML
    private Button btnImprimir;

    @FXML
    private Button btnNuevaVenta;

    @FXML
    private Button btnVolver;

    @FXML
    private Button btnReintentar;

    @FXML
    private TextField txtIdVenta;

    @FXML
    private TextArea txtFactura;

    private final VentaDAO dao = new VentaDAOImpl();

    private FacturaVenta factura;

    private int idVenta;

    @FXML
    private void initialize() {

        PermisosVenta.exigirVendedor();

        if (colTipo != null) {
            colTipo.setCellValueFactory(d
                    -> new ReadOnlyStringWrapper(
                            "boleto".equals(d.getValue().tipoArticulo())
                                    ? "Boleto"
                                    : "Dulcería"
                    ));
        }

        if (colDescripcion != null) {
            colDescripcion.setCellValueFactory(d
                    -> new ReadOnlyStringWrapper(
                            d.getValue().descripcion()
                    ));
        }

        if (colCantidad != null) {
            colCantidad.setCellValueFactory(d
                    -> new ReadOnlyObjectWrapper<>(
                            d.getValue().cantidad()
                    ));
        }

        if (colPrecio != null) {
            colPrecio.setCellValueFactory(d
                    -> new ReadOnlyStringWrapper(
                            Formato.dinero(
                                    d.getValue().precioUnitario()
                            )
                    ));
        }

        if (colSubtotal != null) {
            colSubtotal.setCellValueFactory(d
                    -> new ReadOnlyStringWrapper(
                            Formato.dinero(
                                    d.getValue().subtotal()
                            )
                    ));
        }

        if (tblFactura != null) {
            tblFactura.setPlaceholder(
                    new Label("Cargando factura...")
            );
        }

        if (btnImprimir != null) {
            btnImprimir.setDisable(true);
        }
    }

    public void mostrar(int id) {

        if (id > 0) {
            cargarFactura(id);
        }
    }

    public void cargarFactura(int numero) {

        idVenta = numero;

        if (lblNumero != null) {
            lblNumero.setText("Factura #" + numero);
        }

        if (lblNumeroFactura != null) {
            lblNumeroFactura.setText("#" + numero);
        }

        mensaje(
                "La venta está confirmada. Consultando la factura...",
                false
        );

        TareasFX.ejecutar(
                () -> {

                    FacturaVenta resultado =
                            dao.obtenerFactura(numero);

                    PermisosVenta.exigirAcceso(
                            resultado.venta()
                    );

                    return resultado;
                },
                resultado -> {

                    factura = resultado;

                    VentaResumen venta =
                            resultado.venta();

                    if (lblCliente != null) {
                        lblCliente.setText(
                                venta.cliente()
                        );
                    }

                    if (lblTaquillero != null) {
                        lblTaquillero.setText(
                                venta.taquillero()
                        );
                    }

                    if (lblFecha != null) {
                        lblFecha.setText(
                                venta.fechaVenta().format(
                                        DateTimeFormatter.ofPattern(
                                                "dd/MM/yyyy HH:mm"
                                        )
                                )
                        );
                    }

                    if (lblSubtotalProductos != null) {
                        lblSubtotalProductos.setText(
                                Formato.dinero(
                                        venta.totalProductos()
                                )
                        );
                    }

                    if (lblSubtotalBoletos != null) {
                        lblSubtotalBoletos.setText(
                                Formato.dinero(
                                        venta.totalBoletos()
                                )
                        );
                    }

                    if (lblTotal != null) {
                        lblTotal.setText(
                                Formato.dinero(
                                        venta.totalVenta()
                                )
                        );
                    }

                    if (lblEstado != null) {
                        lblEstado.setText(
                                "CONFIRMADA"
                        );
                    }

                    if (tblFactura != null) {
                        tblFactura.setItems(
                                FXCollections.observableArrayList(
                                        resultado.lineas()
                                )
                        );
                    }

                    if (txtIdVenta != null) {
                        txtIdVenta.setText(
                                String.valueOf(numero)
                        );
                    }

                    if (txtFactura != null) {

                        StringBuilder detalle =
                                new StringBuilder();

                        for (LineaFactura linea
                                : resultado.lineas()) {

                            detalle.append(
                                    linea.descripcion()
                            ).append(" | Cantidad: ")
                                    .append(
                                            linea.cantidad()
                                    )
                                    .append(" | Precio: Q")
                                    .append(
                                            linea.precioUnitario()
                                    )
                                    .append(" | Subtotal: Q")
                                    .append(
                                            linea.subtotal()
                                    )
                                    .append("\n");
                        }

                        txtFactura.setText(
                                detalle.toString()
                        );
                    }

                    if (btnImprimir != null) {
                        btnImprimir.setDisable(false);
                    }

                    mensaje(
                            "Venta confirmada correctamente. Gracias por su compra.",
                            false
                    );
                },
                ex -> {

                    mensaje(
                            "Venta #" + numero
                            + " confirmada. No se pudo cargar la factura: "
                            + Formato.mensaje(ex),
                            true
                    );

                    if (btnImprimir != null) {
                        btnImprimir.setDisable(true);
                    }
                },
                ocupado -> {

                    if (btnReintentar != null) {
                        btnReintentar.setDisable(ocupado);
                    }

                    if (btnNuevaVenta != null) {
                        btnNuevaVenta.setDisable(ocupado);
                    }

                    if (btnVolver != null) {
                        btnVolver.setDisable(ocupado);
                    }

                    if (btnImprimir != null) {
                        btnImprimir.setDisable(ocupado);
                    }
                }
        );
    }

    @FXML
    private void consultar() {

        if (txtIdVenta == null) {
            return;
        }

        String texto =
                txtIdVenta.getText();

        if (texto == null || texto.isBlank()) {

            mostrarAviso(
                    Alert.AlertType.WARNING,
                    "Consulta de factura",
                    "Ingresa el número de factura."
            );

            return;
        }

        try {

            int id =
                    Integer.parseInt(
                            texto.trim()
                    );

            cargarFactura(id);

        } catch (NumberFormatException e) {

            mostrarAviso(
                    Alert.AlertType.ERROR,
                    "Consulta de factura",
                    "El número de venta no es válido."
            );
        }
    }

    @FXML
    private void imprimir() {

        if (factura == null) {

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

    @FXML
    private void reintentar() {

        if (idVenta > 0) {
            cargarFactura(idVenta);
        }
    }

    @FXML
    private void nuevaVenta() throws IOException {

        Principal.mostrarVentas();
    }

    @FXML
    private void nuevaFactura() {

        factura = null;
        idVenta = 0;

        if (txtIdVenta != null) {
            txtIdVenta.clear();
        }

        if (txtFactura != null) {
            txtFactura.clear();
        }

        if (tblFactura != null) {
            tblFactura.getItems().clear();
        }

        if (lblNumero != null) {
            lblNumero.setText("Factura");
        }

        if (lblNumeroFactura != null) {
            lblNumeroFactura.setText("—");
        }

        if (lblCliente != null) {
            lblCliente.setText("—");
        }

        if (lblEstado != null) {
            lblEstado.setText("SIN CONSULTAR");
        }

        if (lblTotal != null) {
            lblTotal.setText("Q0.00");
        }

        if (lblSubtotalBoletos != null) {
            lblSubtotalBoletos.setText("Q0.00");
        }

        if (lblSubtotalProductos != null) {
            lblSubtotalProductos.setText("Q0.00");
        }

        if (btnImprimir != null) {
            btnImprimir.setDisable(true);
        }

        if (txtIdVenta != null) {
            txtIdVenta.requestFocus();
        }
    }

    @FXML
    private void reimprimir() {

        imprimir();
    }

    @FXML
    private void volver() throws IOException {

        Principal.mostrarDashboardSegunRol();
    }

    private void mensaje(
            String texto,
            boolean error) {

        if (lblMensaje == null) {
            return;
        }

        lblMensaje.setText(texto);

        lblMensaje.getStyleClass()
                .remove("mensaje-error");

        if (error) {
            lblMensaje.getStyleClass()
                    .add("mensaje-error");
        }
    }

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