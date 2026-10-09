package org.cine.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import org.cine.dao.GeneroDAO;
import org.cine.dao.PeliculaDAO;
import org.cine.dao.impl.GeneroDAOImpl;
import org.cine.dao.impl.PeliculaDAOImpl;
import org.cine.model.Genero;
import org.cine.model.Pelicula;
import org.cine.system.Principal;

public class PeliculasController {

    @FXML private TextField txtTitulo;
    @FXML private TextArea txtSinopsis;
    @FXML private TextField txtDirector;
    @FXML private TextField txtDuracion;
    @FXML private ComboBox<String> cmbClasificacion;
    @FXML private TextField txtIdioma;
    @FXML private DatePicker dpFechaEstreno;
    @FXML private ComboBox<Genero> cmbGenero;

    @FXML private ImageView imgPoster;
    @FXML private Label lblNombreImagen;
    @FXML private Button btnSeleccionarImagen;
    @FXML private Button btnQuitarImagen;

    @FXML private TableView<Pelicula> tblPeliculas;
    @FXML private TableColumn<Pelicula, Integer> colId;
    @FXML private TableColumn<Pelicula, Image> colImagen;
    @FXML private TableColumn<Pelicula, String> colTitulo;
    @FXML private TableColumn<Pelicula, String> colDirector;
    @FXML private TableColumn<Pelicula, Integer> colDuracion;
    @FXML private TableColumn<Pelicula, String> colClasificacion;
    @FXML private TableColumn<Pelicula, String> colIdioma;
    @FXML private TableColumn<Pelicula, Date> colFechaEstreno;
    @FXML private TableColumn<Pelicula, String> colGenero;
    @FXML private TableColumn<Pelicula, String> colEstado;

    private final PeliculaDAO peliculaDAO = new PeliculaDAOImpl();
    private final GeneroDAO generoDAO = new GeneroDAOImpl();

    private Pelicula peliculaSeleccionada;
    private byte[] imagenSeleccionada;
    private boolean modoEdicion;

    @FXML
    public void initialize() {
        
        configurarClasificacion();
        configurarGenero();
        configurarTabla();
        configurarSeleccion();
        cargarGeneros();
        cargarPeliculas();
        limpiarFormulario();
    }

    private void configurarClasificacion() {
        cmbClasificacion.setItems(FXCollections.observableArrayList(
                "Todo público", "7+", "12+", "15+", "18+"
        ));
    }

    private void configurarGenero() {
        cmbGenero.setConverter(new StringConverter<Genero>() {
            @Override
            public String toString(Genero genero) {
                return genero == null ? "" : genero.getNombreGenero();
            }

            @Override
            public Genero fromString(String texto) {
                return null;
            }
        });
    }

    private void configurarTabla() {

        colId.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getIdPelicula()
                ).asObject());

        colImagen.setCellValueFactory(data -> {
            byte[] bytes = data.getValue().getImagen();

            Image image = null;

            if (bytes != null && bytes.length > 0) {
                image = new Image(
                        new ByteArrayInputStream(bytes),
                        42, 58, true, true
                );
            }

            return new SimpleObjectProperty<>(image);
        });

        colImagen.setCellFactory(column -> new TableCell<>() {

            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(38);
                imageView.setFitHeight(52);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Image image, boolean empty) {
                super.updateItem(image, empty);

                if (empty || image == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(image);
                    setGraphic(imageView);
                }
            }
        });

        colTitulo.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTitulo()
                ));

        colDirector.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDirector() == null
                        ? ""
                        : data.getValue().getDirector()
                ));

        colDuracion.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getDuracionMinutos()
                ).asObject());

        colClasificacion.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getClasificacion()
                ));

        colIdioma.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getIdioma()
                ));

        colFechaEstreno.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getFechaEstreno()
                ));

        colGenero.setCellValueFactory(data ->
                new SimpleStringProperty(
                        nombreGenero(
                                data.getValue().getIdGenero()
                        )
                ));

        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getEstado() == 1
                        ? "Activo"
                        : "Inactivo"
                ));
    }

    private void configurarSeleccion() {
        tblPeliculas.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) {
                        cargarFormulario(actual);
                        modoEdicion = false;
                        configurarCampos(false);
                    }
                });
    }

    private String nombreGenero(int idGenero) {
        for (Genero genero : cmbGenero.getItems()) {
            if (genero.getIdGenero() == idGenero) {
                return genero.getNombreGenero();
            }
        }

        return String.valueOf(idGenero);
    }

    private void cargarGeneros() {
        List<Genero> lista = generoDAO.listarTodos();

        cmbGenero.setItems(
                FXCollections.observableArrayList(lista)
        );
    }

    private void cargarPeliculas() {
        tblPeliculas.setItems(
                FXCollections.observableArrayList(
                        peliculaDAO.listarTodos()
                )
        );
    }

    private void cargarFormulario(Pelicula pelicula) {

        peliculaSeleccionada = pelicula;

        txtTitulo.setText(pelicula.getTitulo());
        txtSinopsis.setText(
                pelicula.getSinopsis() == null
                ? ""
                : pelicula.getSinopsis()
        );

        txtDirector.setText(
                pelicula.getDirector() == null
                ? ""
                : pelicula.getDirector()
        );

        txtDuracion.setText(
                String.valueOf(
                        pelicula.getDuracionMinutos()
                )
        );

        cmbClasificacion.setValue(
                pelicula.getClasificacion()
        );

        txtIdioma.setText(pelicula.getIdioma());

        dpFechaEstreno.setValue(
                pelicula.getFechaEstreno() == null
                ? null
                : pelicula.getFechaEstreno().toLocalDate()
        );

        for (Genero genero : cmbGenero.getItems()) {
            if (genero.getIdGenero()
                    == pelicula.getIdGenero()) {

                cmbGenero.setValue(genero);
                break;
            }
        }

        imagenSeleccionada = pelicula.getImagen();

        mostrarImagen(imagenSeleccionada);

        if (imagenSeleccionada == null
                || imagenSeleccionada.length == 0) {

            lblNombreImagen.setText(
                    "Sin imagen seleccionada"
            );

        } else {
            lblNombreImagen.setText(
                    "Imagen registrada"
            );
        }
    }

    private void configurarCampos(boolean habilitados) {

        txtTitulo.setDisable(!habilitados);
        txtSinopsis.setDisable(!habilitados);
        txtDirector.setDisable(!habilitados);
        txtDuracion.setDisable(!habilitados);
        cmbClasificacion.setDisable(!habilitados);
        txtIdioma.setDisable(!habilitados);
        dpFechaEstreno.setDisable(!habilitados);
        cmbGenero.setDisable(!habilitados);

        btnSeleccionarImagen.setDisable(!habilitados);
        btnQuitarImagen.setDisable(!habilitados);
    }

    @FXML
    private void seleccionarImagen() {

        if (peliculaSeleccionada != null && !modoEdicion) {
            mostrarError(
                    "Modo consulta",
                    "Presione Editar antes de cambiar la imagen."
            );
            return;
        }

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Seleccionar póster");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Imágenes",
                        "*.png", "*.jpg", "*.jpeg"
                )
        );

        File archivo = chooser.showOpenDialog(
                imgPoster.getScene().getWindow()
        );

        if (archivo == null) {
            return;
        }

        try {
            imagenSeleccionada =
                    Files.readAllBytes(archivo.toPath());

            mostrarImagen(imagenSeleccionada);

            lblNombreImagen.setText(
                    archivo.getName()
            );

        } catch (IOException ex) {
            mostrarError(
                    "Error al cargar imagen",
                    ex.getMessage()
            );
        }
    }

    @FXML
    private void quitarImagen() {

        if (peliculaSeleccionada != null && !modoEdicion) {
            mostrarError(
                    "Modo consulta",
                    "Presione Editar antes de quitar la imagen."
            );
            return;
        }

        imagenSeleccionada = null;

        imgPoster.setImage(null);

        lblNombreImagen.setText(
                "Sin imagen seleccionada"
        );
    }

    private void mostrarImagen(byte[] bytes) {

        if (bytes == null || bytes.length == 0) {
            imgPoster.setImage(null);
            return;
        }

        imgPoster.setImage(
                new Image(
                        new ByteArrayInputStream(bytes)
                )
        );
    }

    @FXML
    private void editar() {

        if (peliculaSeleccionada == null) {
            mostrarError(
                    "Seleccione una película",
                    "Seleccione una película de la tabla antes de editar."
            );
            return;
        }

        modoEdicion = true;

        configurarCampos(true);

        mostrarInfo(
                "Modo edición activado",
                "Modifique los datos y presione Guardar película."
        );
    }

    @FXML
    private void guardar() {

        if (peliculaSeleccionada != null && !modoEdicion) {
            mostrarError(
                    "Modo consulta",
                    "Seleccione una película y presione Editar antes de guardar cambios."
            );
            return;
        }

        boolean actualizando =
                modoEdicion && peliculaSeleccionada != null;

        if (!validarDatos(!actualizando)) {
            return;
        }

        Pelicula pelicula = crearDesdeFormulario();

        if (actualizando) {

            pelicula.setIdPelicula(
                    peliculaSeleccionada.getIdPelicula()
            );

            pelicula.setEstado(
                    peliculaSeleccionada.getEstado()
            );

            if (peliculaDAO.actualizar(pelicula)) {

                mostrarInfo(
                        "Película actualizada",
                        "Los cambios se guardaron correctamente."
                );

                cargarPeliculas();
                limpiarFormulario();

            } else {

                mostrarError(
                        "No se pudo actualizar",
                        "Revise la consola para conocer el error de la BD."
                );
            }

        } else {

            pelicula.setEstado(1);

            if (peliculaDAO.insertar(pelicula)) {

                mostrarInfo(
                        "Película guardada",
                        "La película se registró correctamente."
                );

                cargarPeliculas();
                limpiarFormulario();

            } else {

                mostrarError(
                        "No se pudo guardar",
                        "Revise la consola para conocer el error de la BD."
                );
            }
        }
    }

    @FXML
    private void eliminar() {

        if (peliculaSeleccionada == null) {
            mostrarError(
                    "Seleccione una película",
                    "Seleccione una película antes de eliminar."
            );
            return;
        }

        Optional<ButtonType> respuesta = confirmar(
                "Eliminar película",
                "¿Desea eliminar \""
                + peliculaSeleccionada.getTitulo()
                + "\"?"
        );

        if (respuesta.isEmpty()
                || respuesta.get() != ButtonType.OK) {
            return;
        }

        if (peliculaDAO.eliminar(
                peliculaSeleccionada.getIdPelicula())) {

            mostrarInfo(
                    "Operación realizada",
                    "La película fue eliminada. "
                    + "Si tenía historial, se conservó mediante desactivación."
            );

            cargarPeliculas();
            limpiarFormulario();

        } else {

            mostrarError(
                    "No se pudo eliminar",
                    "La base de datos rechazó la operación."
            );
        }
    }

    @FXML
    private void activar() {
        cambiarEstado(1);
    }

    @FXML
    private void desactivar() {
        cambiarEstado(0);
    }

    private void cambiarEstado(int estado) {

        if (peliculaSeleccionada == null) {
            mostrarError(
                    "Seleccione una película",
                    "Seleccione una película de la tabla."
            );
            return;
        }

        if (peliculaDAO.cambiarEstado(
                peliculaSeleccionada.getIdPelicula(),
                estado)) {

            mostrarInfo(
                    "Estado actualizado",
                    estado == 1
                    ? "La película está activa."
                    : "La película está inactiva."
            );

            cargarPeliculas();
            limpiarFormulario();

        } else {

            mostrarError(
                    "No se pudo cambiar el estado",
                    "La BD puede impedir la operación si existen funciones programadas."
            );
        }
    }

    @FXML
    private void limpiar() {
        limpiarFormulario();
    }

    private void limpiarFormulario() {

        peliculaSeleccionada = null;
        imagenSeleccionada = null;
        modoEdicion = false;

        txtTitulo.clear();
        txtSinopsis.clear();
        txtDirector.clear();
        txtDuracion.clear();
        txtIdioma.clear();

        cmbClasificacion.setValue(null);
        cmbGenero.setValue(null);
        dpFechaEstreno.setValue(null);

        imgPoster.setImage(null);

        lblNombreImagen.setText(
                "Sin imagen seleccionada"
        );

        tblPeliculas.getSelectionModel()
                .clearSelection();

        configurarCampos(true);
    }

    @FXML
    private void regresarDashboard()
            throws IOException {

        Principal.mostrarDashboardSegunRol();
    }

    private boolean validarDatos(
            boolean imagenObligatoria) {

        String titulo =
                txtTitulo.getText().trim();

        String director =
                txtDirector.getText().trim();

        String duracionTexto =
                txtDuracion.getText().trim();

        String idioma =
                txtIdioma.getText().trim();

        if (titulo.isEmpty()) {
            mostrarError(
                    "Título obligatorio",
                    "Ingrese el título."
            );
            return false;
        }

        if (director.isEmpty()) {
            mostrarError(
                    "Director obligatorio",
                    "Ingrese el director."
            );
            return false;
        }

        int duracion;

        try {
            duracion =
                    Integer.parseInt(duracionTexto);

        } catch (NumberFormatException ex) {
            mostrarError(
                    "Duración inválida",
                    "Ingrese un número entero."
            );
            return false;
        }

        if (duracion < 1 || duracion > 600) {
            mostrarError(
                    "Duración inválida",
                    "Debe estar entre 1 y 600 minutos."
            );
            return false;
        }

        if (cmbClasificacion.getValue() == null) {
            mostrarError(
                    "Clasificación obligatoria",
                    "Seleccione una clasificación."
            );
            return false;
        }

        if (idioma.isEmpty()) {
            mostrarError(
                    "Idioma obligatorio",
                    "Ingrese el idioma."
            );
            return false;
        }

        if (dpFechaEstreno.getValue() == null) {
            mostrarError(
                    "Fecha obligatoria",
                    "Seleccione la fecha de estreno."
            );
            return false;
        }

        if (cmbGenero.getValue() == null) {
            mostrarError(
                    "Género obligatorio",
                    "Seleccione un género."
            );
            return false;
        }

        if (imagenObligatoria
                && (imagenSeleccionada == null
                || imagenSeleccionada.length == 0)) {

            mostrarError(
                    "Imagen obligatoria",
                    "Seleccione el póster de la película."
            );
            return false;
        }

        int idExcluir =
                peliculaSeleccionada == null
                ? -1
                : peliculaSeleccionada.getIdPelicula();

        if (existeTituloDuplicado(
                titulo, idExcluir)) {

            mostrarError(
                    "Título duplicado",
                    "Ya existe otra película con ese título."
            );
            return false;
        }

        return true;
    }

    private boolean existeTituloDuplicado(
            String titulo,
            int idExcluir) {

        for (Pelicula pelicula :
                peliculaDAO.listarTodos()) {

            if (pelicula.getIdPelicula() != idExcluir
                    && pelicula.getTitulo() != null
                    && pelicula.getTitulo()
                            .equalsIgnoreCase(titulo)) {

                return true;
            }
        }

        return false;
    }

    private Pelicula crearDesdeFormulario() {

        Pelicula pelicula = new Pelicula();

        pelicula.setTitulo(
                txtTitulo.getText().trim()
        );

        pelicula.setSinopsis(
                txtSinopsis.getText().trim()
        );

        pelicula.setDirector(
                txtDirector.getText().trim()
        );

        pelicula.setDuracionMinutos(
                Integer.parseInt(
                        txtDuracion.getText().trim()
                )
        );

        pelicula.setClasificacion(
                cmbClasificacion.getValue()
        );

        pelicula.setIdioma(
                txtIdioma.getText().trim()
        );

        pelicula.setFechaEstreno(
                Date.valueOf(
                        dpFechaEstreno.getValue()
                )
        );

        pelicula.setIdGenero(
                cmbGenero.getValue().getIdGenero()
        );

        pelicula.setImagen(
                imagenSeleccionada
        );

        return pelicula;
    }

    private Optional<ButtonType> confirmar(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Cine");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);

        return alert.showAndWait();
    }

    private void mostrarInfo(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Cine");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }

    private void mostrarError(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Cine");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }
}
