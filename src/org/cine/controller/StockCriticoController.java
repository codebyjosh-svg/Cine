package org.cine.controller;

import java.io.IOException;
import java.util.Locale;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.cine.dao.StockCriticoDAO;
import org.cine.dao.impl.StockCriticoDAOImpl;
import org.cine.model.StockCritico;
import org.cine.service.PermisosVenta;
import org.cine.system.Principal;
import org.cine.util.Formato;
import org.cine.util.TareasFX;

/** Lista, filtra y actualiza los productos con stock crítico sin bloquear JavaFX. */
public final class StockCriticoController {
    @FXML private TableView<StockCritico> tblStockCritico;
    @FXML private TableColumn<StockCritico, Integer> colId, colStock, colMinimo;
    @FXML private TableColumn<StockCritico, String> colProducto, colCategoria, colNivel;
    @FXML private TextField txtFiltro;
    @FXML private ComboBox<String> cmbNivel;
    @FXML private Label lblCriticos, lblAgotados, lblMensaje, lblResultados;
    @FXML private Button btnActualizar, btnVolver;
    private final StockCriticoDAO dao = new StockCriticoDAOImpl();
    private final ObservableList<StockCritico> productos = FXCollections.observableArrayList();
    private final FilteredList<StockCritico> filtrados = new FilteredList<>(productos);

    @FXML
    private void initialize() {
        PermisosVenta.exigirStockCritico();
        colId.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getIdProducto()));
        colStock.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getStock()));
        colMinimo.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getStockMinimo()));
        colProducto.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNombreProducto()));
        colCategoria.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNombreCategoria()));
        colNivel.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getNivel()));
        tblStockCritico.setItems(filtrados);
        tblStockCritico.setPlaceholder(new Label("No hay productos críticos para este filtro."));
        tblStockCritico.setRowFactory(tabla -> new TableRow<>() {
            @Override
            protected void updateItem(StockCritico producto, boolean vacio) {
                super.updateItem(producto, vacio);
                getStyleClass().remove("stock-agotado");
                if (!vacio && producto != null && producto.getStock() == 0) {
                    getStyleClass().add("stock-agotado");
                }
            }
        });
        cmbNivel.setItems(FXCollections.observableArrayList(
                "Todos", "Sin existencias", "Bajo mínimo", "En el mínimo"));
        cmbNivel.getSelectionModel().selectFirst();
        txtFiltro.textProperty().addListener((o, a, n) -> filtrar());
        cmbNivel.valueProperty().addListener((o, a, n) -> filtrar());
        actualizar();
    }

    @FXML
    private void actualizar() {
        lblMensaje.setText("Consultando existencias...");
        lblMensaje.getStyleClass().remove("mensaje-error");
        TareasFX.ejecutar(dao::listar, lista -> {
            productos.setAll(lista);
            lblCriticos.setText(String.valueOf(lista.size()));
            lblAgotados.setText(String.valueOf(lista.stream().filter(p -> p.getStock() == 0).count()));
            filtrar();
            lblMensaje.setText(lista.isEmpty() ? "Todos los productos activos superan su stock mínimo."
                    : "Atención: hay " + lista.size() + " productos que necesitan reposición.");
        }, ex -> {
            productos.clear();
            lblCriticos.setText("—");
            lblAgotados.setText("—");
            filtrar();
            lblMensaje.setText("No se pudo consultar el stock: " + Formato.mensaje(ex));
            lblMensaje.getStyleClass().add("mensaje-error");
        }, ocupado -> {
            btnActualizar.setDisable(ocupado);
            btnVolver.setDisable(ocupado);
        });
    }

    private void filtrar() {
        String texto = txtFiltro.getText().trim().toLowerCase(Locale.ROOT);
        String nivel = cmbNivel.getValue();
        filtrados.setPredicate(p -> (texto.isEmpty()
                || p.getNombreProducto().toLowerCase(Locale.ROOT).contains(texto)
                || p.getNombreCategoria().toLowerCase(Locale.ROOT).contains(texto)
                || String.valueOf(p.getIdProducto()).equals(texto))
                && (nivel == null || "Todos".equals(nivel) || nivel.equals(p.getNivel())));
        lblResultados.setText(filtrados.size() + " productos mostrados");
    }

    @FXML
    private void volver() throws IOException { Principal.mostrarDashboardSegunRol(); }
}
