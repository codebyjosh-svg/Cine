package org.cine.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.cine.dao.VentaDAO;
import org.cine.dao.impl.VentaDAOImpl;
import org.cine.model.ClienteVinculo;
import org.cine.model.LineaVentaProducto;
import org.cine.model.Producto;
import org.cine.model.VentaResumen;
import org.cine.service.PermisosVenta;
import org.cine.system.Principal;
import org.cine.util.Formato;
import org.cine.util.TareasFX;

/** Dulcería y carrito persistido: incrementar, editar, retirar y confirmar. */
public final class VentaController {
    @FXML private ComboBox<ClienteVinculo> cmbCliente;
    @FXML private TextField txtFiltro, txtIdVenta;
    @FXML private Spinner<Integer> spnCantidad, spnCantidadCarrito;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colProducto, colCategoria, colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableView<LineaVentaProducto> tblCarrito;
    @FXML private TableColumn<LineaVentaProducto, String> colCarritoProducto, colCarritoPrecio, colCarritoSubtotal;
    @FXML private TableColumn<LineaVentaProducto, Integer> colCarritoCantidad;
    @FXML private Label lblVenta, lblMensaje, lblSubtotalProductos, lblSubtotalBoletos, lblTotal, lblBoletos;
    @FXML private Button btnAgregar, btnCantidad, btnQuitar, btnConfirmar, btnActualizar,
            btnNueva, btnRetomar, btnVolver, btnVerFactura;
    private final VentaDAO dao = new VentaDAOImpl();
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final FilteredList<Producto> filtrados = new FilteredList<>(productos);
    private int idVenta;
    private boolean ocupado;
    private boolean confirmada;

    private record DatosVenta(VentaResumen venta, List<LineaVentaProducto> lineas, List<Producto> productos) { }
    private record Catalogos(List<ClienteVinculo> clientes, List<Producto> productos) { }

    @FXML
    private void initialize() {
        PermisosVenta.exigirVendedor();
        spnCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1_000_000, 1));
        spnCantidadCarrito.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1_000_000, 1));
        colProducto.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNombreProducto()));
        colCategoria.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNombreCategoria()));
        colPrecio.setCellValueFactory(d -> new ReadOnlyStringWrapper(Formato.dinero(d.getValue().getPrecio())));
        colStock.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getStock()));
        colCarritoProducto.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNombreProducto()));
        colCarritoCantidad.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCantidad()));
        colCarritoPrecio.setCellValueFactory(d -> new ReadOnlyStringWrapper(Formato.dinero(d.getValue().getPrecioUnitario())));
        colCarritoSubtotal.setCellValueFactory(d -> new ReadOnlyStringWrapper(Formato.dinero(d.getValue().getSubtotal())));
        tblProductos.setItems(filtrados);
        tblProductos.setPlaceholder(new Label("No hay productos activos para este filtro."));
        tblCarrito.setPlaceholder(new Label("Seleccione un cliente y agregue productos."));
        txtFiltro.textProperty().addListener((o, a, n) -> {
            String texto = n.trim().toLowerCase(Locale.ROOT);
            filtrados.setPredicate(p -> p.getNombreProducto().toLowerCase(Locale.ROOT).contains(texto)
                    || p.getNombreCategoria().toLowerCase(Locale.ROOT).contains(texto));
        });
        tblProductos.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> actualizarControles());
        tblCarrito.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> {
            if (n != null) { spnCantidadCarrito.getValueFactory().setValue(n.getCantidad()); }
            actualizarControles();
        });
        actualizar();
    }

    @FXML
    private void actualizar() {
        mensaje("Consultando productos y clientes...", false);
        if (idVenta == 0) {
            operar(() -> new Catalogos(dao.listarClientesActivos(), dao.listarProductosDisponibles()), datos -> {
                ClienteVinculo anterior = cmbCliente.getValue();
                cmbCliente.setItems(FXCollections.observableArrayList(datos.clientes()));
                if (anterior != null) {
                    datos.clientes().stream().filter(c -> c.getIdCliente() == anterior.getIdCliente())
                            .findFirst().ifPresent(c -> cmbCliente.setValue(c));
                }
                mostrarProductos(datos.productos());
                mensaje("Seleccione un cliente y un producto para comenzar.", false);
            });
        } else {
            int numero = idVenta;
            operar(() -> consultar(numero), datos -> {
                aplicar(datos);
                mensaje("Existencias y carrito actualizados.", false);
            });
        }
    }

    @FXML
    private void agregar() {
        Producto producto = tblProductos.getSelectionModel().getSelectedItem();
        if (producto == null) { mensaje("Seleccione un producto de dulcería.", true); return; }
        int cantidad = spnCantidad.getValue();
        long acumulada = tblCarrito.getItems().stream()
                .filter(l -> l.getIdProducto() == producto.getIdProducto())
                .mapToLong(LineaVentaProducto::getCantidad).sum() + cantidad;
        if (acumulada > producto.getStock()) {
            mensaje("Stock insuficiente para " + producto.getNombreProducto()
                    + ". Disponible: " + producto.getStock() + "; solicitado: " + acumulada + ".", true);
            return;
        }
        if (idVenta == 0) {
            ClienteVinculo cliente = cmbCliente.getValue();
            if (cliente == null) { mensaje("Seleccione un cliente antes de agregar productos.", true); return; }
            int vendedor = PermisosVenta.exigirVendedor().getIdUsuario();
            operar(() -> dao.abrirVenta(cliente.getIdCliente(), vendedor), numero -> {
                // Se conserva el ID aunque el siguiente agregado sea rechazado por un cambio de stock.
                idVenta = numero;
                txtIdVenta.setText(String.valueOf(numero));
                lblVenta.setText("Venta #" + numero + " · abierta");
                agregarProducto(producto.getIdProducto(), cantidad);
            });
        } else {
            agregarProducto(producto.getIdProducto(), cantidad);
        }
    }

    private void agregarProducto(int idProducto, int cantidad) {
        int numero = idVenta;
        operar(() -> {
            dao.agregarProducto(numero, idProducto, cantidad);
            return consultar(numero);
        }, datos -> {
            aplicar(datos);
            mensaje("Producto agregado. La cantidad se acumula en una sola línea.", false);
        });
    }

    @FXML
    private void guardarCantidad() {
        LineaVentaProducto linea = tblCarrito.getSelectionModel().getSelectedItem();
        if (linea == null) { mensaje("Seleccione una línea del carrito.", true); return; }
        int numero = idVenta;
        int cantidad = spnCantidadCarrito.getValue();
        operar(() -> {
            dao.cambiarCantidad(numero, linea.getIdProducto(), cantidad);
            return consultar(numero);
        }, datos -> { aplicar(datos); mensaje("Cantidad actualizada.", false); });
    }

    @FXML
    private void quitar() {
        LineaVentaProducto linea = tblCarrito.getSelectionModel().getSelectedItem();
        if (linea == null) { mensaje("Seleccione una línea del carrito.", true); return; }
        int numero = idVenta;
        operar(() -> {
            dao.quitarProducto(numero, linea.getIdProducto());
            return consultar(numero);
        }, datos -> { aplicar(datos); mensaje("Producto retirado del carrito.", false); });
    }

    @FXML
    private void retomar() {
        try {
            int numero = Integer.parseInt(txtIdVenta.getText().trim());
            if (numero <= 0) { throw new NumberFormatException(); }
            operar(() -> {
                DatosVenta datos = consultar(numero);
                if (!"abierta".equals(datos.venta().estado())) {
                    throw new IllegalStateException("Solo puede retomar una venta abierta.");
                }
                return datos;
            }, datos -> { aplicar(datos); mensaje("Venta retomada. Los boletos conservan su subtotal.", false); });
        } catch (NumberFormatException ex) {
            mensaje("Ingrese el número de una venta abierta.", true);
        }
    }

    @FXML
    private void confirmar() {
        if (idVenta == 0 || cmbCliente.getValue() == null) {
            mensaje("Seleccione un cliente y agregue artículos antes de confirmar.", true);
            return;
        }
        int numero = idVenta;
        operar(() -> { dao.confirmarVenta(numero); return numero; }, guardada -> {
            confirmada = true;
            actualizarControles();
            lblVenta.setText("Venta #" + guardada + " · confirmada");
            mensaje("Venta #" + guardada + " confirmada correctamente.", false);
            verFactura();
        });
    }

    @FXML
    private void verFactura() {
        try { Principal.mostrarFactura(idVenta); }
        catch (IOException | IllegalStateException ex) {
            mensaje("La venta #" + idVenta + " está confirmada. No se pudo abrir la factura: "
                    + Formato.mensaje(ex) + ". Use Ver factura para reintentar.", true);
        }
    }

    @FXML
    private void nueva() {
        int anterior = idVenta;
        boolean estabaConfirmada = confirmada;
        idVenta = 0;
        confirmada = false;
        cmbCliente.getSelectionModel().clearSelection();
        tblCarrito.getItems().clear();
        txtIdVenta.clear();
        lblVenta.setText("Nueva venta");
        totales(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
        actualizarControles();
        mensaje(anterior > 0 && !estabaConfirmada
                ? "La venta #" + anterior + " permanece abierta. Puede retomarla por su número."
                : "Seleccione un cliente para una nueva venta.", false);
    }

    @FXML
    private void volver() throws IOException { Principal.mostrarDashboardSegunRol(); }

    private DatosVenta consultar(int numero) throws Exception {
        VentaResumen venta = dao.buscarVenta(numero);
        PermisosVenta.exigirAcceso(venta);
        return new DatosVenta(venta, dao.listarProductosVenta(numero), dao.listarProductosDisponibles());
    }

    private void aplicar(DatosVenta datos) {
        VentaResumen venta = datos.venta();
        idVenta = venta.idVenta();
        confirmada = !"abierta".equals(venta.estado());
        lblVenta.setText("Venta #" + idVenta + " · " + venta.estado());
        txtIdVenta.setText(String.valueOf(idVenta));
        ClienteVinculo cliente = cmbCliente.getItems().stream()
                .filter(c -> c.getIdCliente() == venta.idCliente()).findFirst()
                .orElse(new ClienteVinculo(venta.idCliente(), venta.cliente(), "", true));
        cmbCliente.setValue(cliente);
        LineaVentaProducto seleccionada = tblCarrito.getSelectionModel().getSelectedItem();
        tblCarrito.setItems(FXCollections.observableArrayList(datos.lineas()));
        if (seleccionada != null) {
            datos.lineas().stream().filter(l -> l.getIdProducto() == seleccionada.getIdProducto())
                    .findFirst().ifPresent(l -> tblCarrito.getSelectionModel().select(l));
        }
        mostrarProductos(datos.productos());
        totales(venta.totalProductos(), venta.totalBoletos(), venta.totalVenta(), venta.cantidadBoletos());
        actualizarControles();
    }

    private void mostrarProductos(List<Producto> lista) {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        productos.setAll(lista);
        if (seleccionado != null) {
            lista.stream().filter(p -> p.getIdProducto() == seleccionado.getIdProducto())
                    .findFirst().ifPresent(p -> {
                        tblProductos.getSelectionModel().select(p);
                        tblProductos.scrollTo(p);
                    });
        }
    }

    private void totales(BigDecimal productosTotal, BigDecimal boletosTotal, BigDecimal total, int boletos) {
        lblSubtotalProductos.setText(Formato.dinero(productosTotal));
        lblSubtotalBoletos.setText(Formato.dinero(boletosTotal));
        lblTotal.setText(Formato.dinero(total));
        lblBoletos.setText(boletos + " boletos en esta venta");
    }

    private <T> void operar(Callable<T> trabajo, Consumer<T> exito) {
        if (ocupado) { return; }
        PermisosVenta.exigirVendedor();
        TareasFX.ejecutar(trabajo, exito, ex -> mensaje(Formato.mensaje(ex), true), valor -> {
            ocupado = valor;
            actualizarControles();
        });
    }

    private void actualizarControles() {
        boolean editable = !ocupado && !confirmada;
        btnAgregar.setDisable(!editable || tblProductos.getSelectionModel().getSelectedItem() == null);
        btnCantidad.setDisable(!editable || tblCarrito.getSelectionModel().getSelectedItem() == null);
        btnQuitar.setDisable(btnCantidad.isDisable());
        btnConfirmar.setDisable(!editable || idVenta == 0);
        cmbCliente.setDisable(ocupado || idVenta != 0);
        btnActualizar.setDisable(ocupado);
        btnNueva.setDisable(ocupado);
        btnRetomar.setDisable(ocupado);
        btnVolver.setDisable(ocupado);
        btnVerFactura.setDisable(ocupado || !confirmada || idVenta == 0);
        spnCantidad.setDisable(!editable);
        spnCantidadCarrito.setDisable(!editable);
        txtIdVenta.setDisable(ocupado);
    }

    private void mensaje(String texto, boolean error) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().remove("mensaje-error");
        if (error) { lblMensaje.getStyleClass().add("mensaje-error"); }
    }
}
