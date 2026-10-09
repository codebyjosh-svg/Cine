package org.cine.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cine.dao.VentaDAO;
import org.cine.dao.impl.VentaDAOImpl;
import org.cine.model.FacturaVenta;
import org.cine.model.LineaFactura;
import org.cine.model.VentaResumen;
import org.cine.service.PermisosVenta;
import org.cine.system.Principal;
import org.cine.util.Formato;
import org.cine.util.TareasFX;

/** Muestra los importes persistidos e imprime en la impresora predeterminada sin diálogo. */
public final class FacturaController {
    @FXML private Label lblNumero, lblFecha, lblCliente, lblTaquillero, lblMensaje,
            lblSubtotalProductos, lblSubtotalBoletos, lblTotal;
    @FXML private TableView<LineaFactura> tblFactura;
    @FXML private TableColumn<LineaFactura, String> colTipo, colDescripcion, colPrecio, colSubtotal;
    @FXML private TableColumn<LineaFactura, Integer> colCantidad;
    @FXML private Button btnImprimir, btnNuevaVenta, btnVolver, btnReintentar;
    private final VentaDAO dao = new VentaDAOImpl();
    private FacturaVenta factura;
    private int idVenta;

    @FXML
    private void initialize() {
        PermisosVenta.exigirVendedor();
        colTipo.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                "boleto".equals(d.getValue().tipoArticulo()) ? "Boleto" : "Dulcería"));
        colDescripcion.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().descripcion()));
        colCantidad.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().cantidad()));
        colPrecio.setCellValueFactory(d -> new ReadOnlyStringWrapper(Formato.dinero(d.getValue().precioUnitario())));
        colSubtotal.setCellValueFactory(d -> new ReadOnlyStringWrapper(Formato.dinero(d.getValue().subtotal())));
        tblFactura.setPlaceholder(new Label("Cargando factura..."));
        btnImprimir.setDisable(true);
    }

    public void cargarFactura(int numero) {
        idVenta = numero;
        lblNumero.setText("Factura #" + numero);
        mensaje("La venta está confirmada. Consultando la factura...", false);
        TareasFX.ejecutar(() -> {
            FacturaVenta resultado = dao.obtenerFactura(numero);
            PermisosVenta.exigirAcceso(resultado.venta());
            return resultado;
        }, resultado -> {
            factura = resultado;
            VentaResumen venta = resultado.venta();
            lblCliente.setText(venta.cliente());
            lblTaquillero.setText(venta.taquillero());
            lblFecha.setText(venta.fechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            lblSubtotalProductos.setText(Formato.dinero(venta.totalProductos()));
            lblSubtotalBoletos.setText(Formato.dinero(venta.totalBoletos()));
            lblTotal.setText(Formato.dinero(venta.totalVenta()));
            tblFactura.setItems(FXCollections.observableArrayList(resultado.lineas()));
            btnImprimir.setDisable(false);
            mensaje("Venta confirmada correctamente. Gracias por su compra.", false);
        }, ex -> {
            mensaje("Venta #" + numero + " confirmada. No se pudo cargar la factura: " + Formato.mensaje(ex), true);
            btnImprimir.setDisable(true);
        }, ocupado -> {
            btnReintentar.setDisable(ocupado);
            btnNuevaVenta.setDisable(ocupado);
            btnVolver.setDisable(ocupado);
            btnImprimir.setDisable(true);
        });
    }

    @FXML private void reintentar() { cargarFactura(idVenta); }
    @FXML private void nuevaVenta() throws IOException { Principal.mostrarVentas(); }
    @FXML private void volver() throws IOException { Principal.mostrarDashboardSegunRol(); }

    @FXML
    private void imprimir() {
        if (factura == null) { return; }
        PrinterJob trabajo = PrinterJob.createPrinterJob();
        if (trabajo == null) {
            mensaje("No hay una impresora predeterminada disponible.", true);
            return;
        }
        try {
            PageLayout formato = trabajo.getJobSettings().getPageLayout();
            for (VBox pagina : paginasImpresion(formato)) {
                if (!trabajo.printPage(formato, pagina)) {
                    trabajo.cancelJob();
                    mensaje("No se pudo imprimir la factura.", true);
                    return;
                }
            }
            boolean finalizado = trabajo.endJob();
            mensaje(finalizado ? "Impresión correcta."
                    : "No se pudo finalizar la impresión.", !finalizado);
        } catch (RuntimeException ex) {
            trabajo.cancelJob();
            mensaje("No se pudo imprimir: " + Formato.mensaje(ex), true);
        }
    }

    /** Construye páginas desde los datos; imprime todas las filas, incluso fuera de la tabla visible. */
    private List<VBox> paginasImpresion(PageLayout formato) {
        List<VBox> paginas = new ArrayList<>();
        double ancho = formato.getPrintableWidth();
        double alto = formato.getPrintableHeight();
        VBox pagina = encabezado(ancho);
        paginas.add(pagina);
        double usado = 125;
        for (LineaFactura linea : factura.lineas()) {
            Text texto = new Text(linea.descripcion() + "\n"
                    + linea.cantidad() + " x " + Formato.dinero(linea.precioUnitario())
                    + "    Subtotal: " + Formato.dinero(linea.subtotal()));
            texto.setFont(Font.font("Arial", 11));
            texto.setWrappingWidth(ancho);
            double altura = texto.getLayoutBounds().getHeight() + 8;
            if (usado + altura > alto - 95) {
                pagina = encabezado(ancho);
                paginas.add(pagina);
                usado = 125;
            }
            pagina.getChildren().add(texto);
            usado += altura;
        }
        VentaResumen venta = factura.venta();
        pagina.getChildren().add(new Text("\nSubtotal boletos: " + Formato.dinero(venta.totalBoletos())
                + "\nSubtotal dulcería: " + Formato.dinero(venta.totalProductos())
                + "\nTOTAL: " + Formato.dinero(venta.totalVenta()) + "\nGracias por su compra."));
        for (int i = 0; i < paginas.size(); i++) {
            paginas.get(i).getChildren().add(new Text("Página " + (i + 1) + " de " + paginas.size()));
        }
        return paginas;
    }

    private VBox encabezado(double ancho) {
        VentaResumen venta = factura.venta();
        VBox pagina = new VBox(8);
        pagina.setPrefWidth(ancho);
        Text titulo = new Text("CINEMA · FACTURA #" + venta.idVenta());
        titulo.setFont(Font.font("Arial", 17));
        Text datos = new Text("Fecha: " + lblFecha.getText() + "\nCliente: " + venta.cliente()
                + "\nTaquillero: " + venta.taquillero());
        datos.setWrappingWidth(ancho);
        pagina.getChildren().addAll(titulo, datos);
        return pagina;
    }

    private void mensaje(String texto, boolean error) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().remove("mensaje-error");
        if (error) { lblMensaje.getStyleClass().add("mensaje-error"); }
    }
}
