package org.cine.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.cine.dao.CategoriaProductoDAO;
import org.cine.dao.ProductoDAO;
import org.cine.dao.impl.CategoriaProductoDAOImpl;
import org.cine.dao.impl.ProductoDAOImpl;
import org.cine.model.CategoriaProducto;
import org.cine.model.Producto;
import org.cine.system.Principal;
import org.cine.util.ValidacionDulceria;

public class ProductoController {

    @FXML private ComboBox<CategoriaProducto> cboCategoriaProducto;
    @FXML private TextField txtNombreProducto;
    @FXML private TextField txtPrecioProducto;
    @FXML private TextField txtStockProducto;
    @FXML private TextField txtStockMinimoProducto;
    @FXML private TextArea txtDescripcionProducto;
    @FXML private Label lblEstadoProducto;

    @FXML private TableView<Producto> tbProductos;
    @FXML private TableColumn<Producto, Integer> colIdProducto;
    @FXML private TableColumn<Producto, String> colNombreCategoria;
    @FXML private TableColumn<Producto, String> colNombreProducto;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioProducto;
    @FXML private TableColumn<Producto, Integer> colStockProducto;
    @FXML private TableColumn<Producto, Integer> colStockMinimoProducto;
    @FXML private TableColumn<Producto, String> colEstadoProducto;

    private final CategoriaProductoDAO categoriaDAO =
            new CategoriaProductoDAOImpl();

    private final ProductoDAO productoDAO =
            new ProductoDAOImpl();

    private Producto productoSeleccionado;

    @FXML
    private void initialize() {
        configurarTabla();

        tbProductos.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, actual) ->
                        seleccionarProducto());

        cargarCategoriasActivas();
        cargarProductos();
    }

    private void configurarTabla() {
        colIdProducto.setCellValueFactory(
                new PropertyValueFactory<>("idProducto")
        );

        colNombreCategoria.setCellValueFactory(
                new PropertyValueFactory<>("nombreCategoria")
        );

        colNombreProducto.setCellValueFactory(
                new PropertyValueFactory<>("nombreProducto")
        );

        colPrecioProducto.setCellValueFactory(
                new PropertyValueFactory<>("precio")
        );

        colStockProducto.setCellValueFactory(
                new PropertyValueFactory<>("stock")
        );

        colStockMinimoProducto.setCellValueFactory(
                new PropertyValueFactory<>("stockMinimo")
        );

        colEstadoProducto.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getEstado() == 1
                                ? "Activo" : "Inactivo"
                )
        );
    }

    private void cargarCategoriasActivas() {
        List<CategoriaProducto> categorias =
                categoriaDAO.listarTodos();

        List<CategoriaProducto> activas = new ArrayList<>();

        for (CategoriaProducto categoria : categorias) {
            if (categoria.getEstado() == 1) {
                activas.add(categoria);
            }
        }

        cboCategoriaProducto.setItems(
                FXCollections.observableArrayList(activas)
        );

        String error = categoriaDAO.getUltimoError();

        if (error != null && !error.isBlank()) {
            mostrarMensaje(
                    Alert.AlertType.ERROR,
                    "No se pudieron cargar las categorías.\n" + error
            );
        }
    }

    @FXML
    private void cargarProductos() {
        List<Producto> productos = productoDAO.listarTodos();

        tbProductos.setItems(
                FXCollections.observableArrayList(productos)
        );

        String error = productoDAO.getUltimoError();

        if (error != null && !error.isBlank()) {
            mostrarMensaje(
                    Alert.AlertType.ERROR,
                    "No se pudieron cargar los productos.\n" + error
            );
        }
    }

    @FXML
    private void seleccionarProducto() {
        productoSeleccionado =
                tbProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            return;
        }

        txtStockProducto.setEditable(false);
        txtStockProducto.setPromptText(
                "Cambie el stock desde Inventario"
        );

        txtNombreProducto.setText(
                productoSeleccionado.getNombreProducto()
        );

        txtPrecioProducto.setText(
                productoSeleccionado.getPrecio() == null
                        ? ""
                        : productoSeleccionado
                                .getPrecio().toPlainString()
        );

        txtStockProducto.setText(
                String.valueOf(productoSeleccionado.getStock())
        );

        txtStockMinimoProducto.setText(
                String.valueOf(
                        productoSeleccionado.getStockMinimo()
                )
        );

        txtDescripcionProducto.setText(
                productoSeleccionado.getDescripcion() == null
                        ? ""
                        : productoSeleccionado.getDescripcion()
        );

        lblEstadoProducto.setText(
                productoSeleccionado.getEstado() == 1
                        ? "Activo" : "Inactivo"
        );

        cargarCategoriasActivas();
        cboCategoriaProducto.getSelectionModel().clearSelection();

        boolean categoriaIncluida =
                cboCategoriaProducto.getItems().stream()
                        .anyMatch(c ->
                                c.getIdCategoriaProducto()
                                == productoSeleccionado
                                        .getIdCategoriaProducto()
                        );

        // Permite conservar la categoría original aunque esté inactiva.
        if (!categoriaIncluida) {
            CategoriaProducto actual =
                    categoriaDAO.buscarCategoriaProducto(
                            productoSeleccionado
                                    .getIdCategoriaProducto()
                    );

            if (actual != null) {
                cboCategoriaProducto.getItems().add(actual);
            }
        }

        for (CategoriaProducto categoria :
                cboCategoriaProducto.getItems()) {

            if (categoria.getIdCategoriaProducto()
                    == productoSeleccionado.getIdCategoriaProducto()) {

                cboCategoriaProducto.setValue(categoria);
                break;
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
        txtStockProducto.setEditable(true);
        productoSeleccionado = null;

        tbProductos.getSelectionModel().clearSelection();
        cboCategoriaProducto.getSelectionModel().clearSelection();

        txtNombreProducto.clear();
        txtPrecioProducto.clear();
        txtStockProducto.clear();
        txtStockMinimoProducto.clear();
        txtDescripcionProducto.clear();

        lblEstadoProducto.setText(
                "Nuevo - se guardará como Activo"
        );
    }

    @FXML
    private void guardarProducto() {
        if (productoSeleccionado != null) {
            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    "Pulse Limpiar para registrar un producto nuevo. "
                    + "Use Editar para modificar el seleccionado."
            );
            return;
        }

        try {
            resultado(
                    productoDAO.insertar(leerFormulario()),
                    "Producto registrado correctamente."
            );

        } catch (IllegalArgumentException ex) {
            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    ex.getMessage()
            );
        }
    }

    @FXML
    private void editarProducto() {
        if (!haySeleccion()) {
            return;
        }

        try {
            resultado(
                    productoDAO.actualizar(leerFormulario()),
                    "Producto actualizado correctamente."
            );

        } catch (IllegalArgumentException ex) {
            mostrarMensaje(
                    Alert.AlertType.WARNING,
                    ex.getMessage()
            );
        }
    }

    @FXML
    private void eliminarProducto() {
        if (!haySeleccion()) {
            return;
        }

        Alert confirmar = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Eliminar definitivamente el producto seleccionado? "
                + "Si tiene datos relacionados, utilice Desactivar.",
                ButtonType.YES,
                ButtonType.NO
        );

        confirmar.setHeaderText("Confirmar eliminación");

        ButtonType respuesta =
                confirmar.showAndWait().orElse(ButtonType.NO);

        if (respuesta == ButtonType.YES) {
            resultado(
                    productoDAO.eliminar(
                            productoSeleccionado.getIdProducto()
                    ),
                    "Producto eliminado correctamente."
            );
        }
    }

    @FXML
    private void activarProducto() {
        cambiarEstado(1);
    }

    @FXML
    private void desactivarProducto() {
        cambiarEstado(0);
    }

    private void cambiarEstado(int estado) {
        if (!haySeleccion()) {
            return;
        }

        if (productoSeleccionado.getEstado() == estado) {
            mostrarMensaje(
                    Alert.AlertType.INFORMATION,
                    "El producto ya tiene ese estado."
            );
            return;
        }

        resultado(
                productoDAO.cambiarEstado(
                        productoSeleccionado.getIdProducto(),
                        estado
                ),
                estado == 1
                        ? "Producto activado correctamente."
                        : "Producto desactivado correctamente."
        );
    }

    private boolean haySeleccion() {
        if (productoSeleccionado != null) {
            return true;
        }

        mostrarMensaje(
                Alert.AlertType.WARNING,
                "Seleccione un producto de la tabla."
        );

        return false;
    }

    private Producto leerFormulario() {
        CategoriaProducto categoria =
                cboCategoriaProducto.getValue();

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "Seleccione una categoría."
            );
        }

        if (categoria.getEstado() != 1
                && (productoSeleccionado == null
                || categoria.getIdCategoriaProducto()
                != productoSeleccionado.getIdCategoriaProducto())) {

            throw new IllegalArgumentException(
                    "Seleccione una categoría activa."
            );
        }

        Producto producto = new Producto();

        producto.setIdCategoriaProducto(
                categoria.getIdCategoriaProducto()
        );

        producto.setNombreProducto(
                ValidacionDulceria.nombre(
                        txtNombreProducto.getText()
                )
        );

        producto.setDescripcion(
                ValidacionDulceria.descripcion(
                        txtDescripcionProducto.getText()
                )
        );

        producto.setPrecio(
                ValidacionDulceria.precio(
                        txtPrecioProducto.getText()
                )
        );

        producto.setStock(
                productoSeleccionado == null
                        ? ValidacionDulceria.entero(
                                txtStockProducto.getText(),
                                "Stock"
                        )
                        : productoSeleccionado.getStock()
        );

        producto.setStockMinimo(
                ValidacionDulceria.entero(
                        txtStockMinimoProducto.getText(),
                        "Stock mínimo"
                )
        );

        if (productoSeleccionado != null) {
            producto.setIdProducto(
                    productoSeleccionado.getIdProducto()
            );

            producto.setEstado(
                    productoSeleccionado.getEstado()
            );
        }

        return producto;
    }

    private void resultado(boolean correcto, String mensaje) {
        if (!correcto) {
            mostrarMensaje(
                    Alert.AlertType.ERROR,
                    productoDAO.getUltimoError()
            );
            return;
        }

        limpiarFormulario();
        cargarProductos();

        mostrarMensaje(
                Alert.AlertType.INFORMATION,
                mensaje
        );
    }

    @FXML
    private void regresarDashboard() throws IOException {
        Principal.mostrarDashboardSegunRol();
    }

    private void mostrarMensaje(
            Alert.AlertType tipo,
            String mensaje) {

        Alert alerta = new Alert(tipo);

        alerta.setTitle("Productos de dulcería");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}