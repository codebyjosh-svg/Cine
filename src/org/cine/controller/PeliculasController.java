package org.cine.controller;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import org.cine.dao.GeneroDAO;
import org.cine.dao.PeliculaDAO;
import org.cine.dao.impl.GeneroDAOImpl;
import org.cine.dao.impl.PeliculaDAOImpl;
import org.cine.model.Genero;
import org.cine.model.Pelicula;

public class PeliculasController {

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextArea txtSinopsis;

    @FXML
    private TextField txtDirector;

    @FXML
    private TextField txtDuracion;

    @FXML
    private ComboBox<String> cmbClasificacion;

    @FXML
    private TextField txtIdioma;

    @FXML
    private DatePicker dpFechaEstreno;

    @FXML
    private ComboBox<Genero> cmbGenero;

    @FXML
    private TableView<Pelicula> tblPeliculas;

    @FXML
    private TableColumn<Pelicula, Integer> colId;

    @FXML
    private TableColumn<Pelicula, String> colTitulo;

    @FXML
    private TableColumn<Pelicula, String> colDirector;

    @FXML
    private TableColumn<Pelicula, Integer> colDuracion;

    @FXML
    private TableColumn<Pelicula, String> colClasificacion;

    @FXML
    private TableColumn<Pelicula, String> colIdioma;

    @FXML
    private TableColumn<Pelicula, java.sql.Date> colFechaEstreno;

    @FXML
    private TableColumn<Pelicula, String> colGenero;

    @FXML
    private TableColumn<Pelicula, String> colEstado;

    private PeliculaDAO peliculaDAO = new PeliculaDAOImpl();

    private GeneroDAO generoDAO = new GeneroDAOImpl();

    @FXML
    public void initialize() {

        configurarTabla();

        configurarGenero();

        cargarGeneros();

        cargarPeliculas();
    }

    private void configurarTabla() {

        colId.setCellValueFactory(
                datos -> new javafx.beans.property.SimpleIntegerProperty(
                        datos.getValue().getIdPelicula()
                ).asObject()
        );

        colTitulo.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        datos.getValue().getTitulo()
                )
        );

        colDirector.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        datos.getValue().getDirector()
                )
        );

        colDuracion.setCellValueFactory(
                datos -> new javafx.beans.property.SimpleIntegerProperty(
                        datos.getValue().getDuracionMinutos()
                ).asObject()
        );

        colClasificacion.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        datos.getValue().getClasificacion()
                )
        );

        colIdioma.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        datos.getValue().getIdioma()
                )
        );

        colFechaEstreno.setCellValueFactory(
                datos -> new javafx.beans.property.SimpleObjectProperty<>(
                        datos.getValue().getFechaEstreno()
                )
        );

        colGenero.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        String.valueOf(
                                datos.getValue().getIdGenero()
                        )
                )
        );

        colEstado.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        datos.getValue().getEstado() == 1
                                ? "Activo"
                                : "Inactivo"
                )
        );
    }

    private void configurarGenero() {

        cmbGenero.setConverter(
                new StringConverter<Genero>() {

                    @Override
                    public String toString(Genero genero) {

                        if (genero == null) {
                            return "";
                        }

                        return genero.getNombreGenero();
                    }

                    @Override
                    public Genero fromString(String texto) {
                        return null;
                    }
                }
        );
    }

    private void cargarGeneros() {

        List<Genero> lista = generoDAO.listarTodos();

        cmbGenero.setItems(
                FXCollections.observableArrayList(lista)
        );
    }

    private void cargarPeliculas() {

        List<Pelicula> lista = peliculaDAO.listarTodos();

        tblPeliculas.setItems(
                FXCollections.observableArrayList(lista)
        );
    }
}