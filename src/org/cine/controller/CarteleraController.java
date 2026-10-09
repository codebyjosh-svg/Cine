package org.cine.controller;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import org.cine.dao.CarteleraDAO;
import org.cine.dao.impl.CarteleraDAOImpl;
import org.cine.model.CarteleraItem;

/**
 * Controlador de la pantalla de Cartelera.
 *
 * Se encarga de cargar y mostrar las funciones disponibles
 * obtenidas desde la base de datos.
 *
 * @author Joshua
 */
public class CarteleraController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private ComboBox<?> cbGenero;

    @FXML
    private ComboBox<?> cbSala;

    @FXML
    private Button btnVolver;

    @FXML
    private Button btnFiltrar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private TilePane gridCartelera;

    private final CarteleraDAO carteleraDAO =
            new CarteleraDAOImpl();

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Inicializa la pantalla.
     */
    @FXML
    public void initialize() {
        cargarCartelera();
    }

    /**
     * Carga las funciones disponibles desde la base de datos
     * y las muestra en la cartelera.
     */
    private void cargarCartelera() {

        try {

            List<CarteleraItem> lista =
                    carteleraDAO.listarCartelera();

            gridCartelera.getChildren().clear();

            if (lista == null || lista.isEmpty()) {
                mostrarSinResultados();
                return;
            }

            for (CarteleraItem item : lista) {
                gridCartelera.getChildren()
                        .add(crearTarjeta(item));
            }

        } catch (SQLException ex) {

            gridCartelera.getChildren().clear();

            Label error = new Label(
                    "No se pudo cargar la cartelera."
            );

            error.setStyle(
                    "-fx-text-fill: #b91c1c;"
                    + "-fx-font-size: 13px;"
                    + "-fx-font-weight: bold;"
            );

            gridCartelera.getChildren().add(error);

            ex.printStackTrace();
        }
    }

    /**
     * Crea visualmente una tarjeta de cartelera
     * utilizando los datos reales de la base de datos.
     *
     * @param item función de cartelera
     * @return tarjeta visual
     */
    private VBox crearTarjeta(CarteleraItem item) {

        VBox tarjeta = new VBox(0);

        tarjeta.setPrefWidth(275);
        tarjeta.setMaxWidth(275);

        tarjeta.getStyleClass().add("movie-card");

        /*
         * ==============================
         * POSTER
         * ==============================
         */

        StackPane posterContainer =
                new StackPane();

        posterContainer
                .getStyleClass()
                .add("poster-container");

        ImageView poster =
                new ImageView();

        poster.setFitWidth(257);
        poster.setFitHeight(185);
        poster.setPreserveRatio(true);

        poster.getStyleClass()
                .add("movie-poster");

        cargarPoster(
                poster,
                item.getIdPelicula()
        );

        posterContainer
                .getChildren()
                .add(poster);

        /*
         * ==============================
         * BADGES
         * ==============================
         */

        HBox badges = new HBox(4);

        badges.setPadding(
                new Insets(6)
        );

        badges.setStyle(
                "-fx-alignment: top-right;"
        );

        StackPane.setAlignment(
                badges,
                javafx.geometry.Pos.TOP_RIGHT
        );

        Label clasificacion =
                new Label(
                        "Clasificación "
                        + valorTexto(
                                item.getClasificacion()
                        )
                );

        clasificacion
                .getStyleClass()
                .add("classification-badge");

        Label genero =
                new Label(
                        valorTexto(
                                item.getGenero()
                        )
                );

        genero
                .getStyleClass()
                .add("genre-badge");

        badges.getChildren()
                .addAll(
                        clasificacion,
                        genero
                );

        posterContainer
                .getChildren()
                .add(badges);

        /*
         * ==============================
         * INFORMACIÓN
         * ==============================
         */

        VBox informacion =
                new VBox(4);

        informacion
                .getStyleClass()
                .add("movie-info");

        /*
         * TÍTULO Y PRECIO
         */

        HBox tituloPrecio =
                new HBox();

        tituloPrecio.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
        );

        Label titulo =
                new Label(
                        valorTexto(
                                item.getTituloPelicula()
                        )
                );

        titulo
                .getStyleClass()
                .add("movie-title");

        titulo.setWrapText(true);

        Region espacio =
                new Region();

        HBox.setHgrow(
                espacio,
                javafx.scene.layout.Priority.ALWAYS
        );

        Label precio =
                new Label(
                        formatearPrecio(
                                item.getPrecio()
                        )
                );

        precio
                .getStyleClass()
                .add("movie-price");

        tituloPrecio.getChildren()
                .addAll(
                        titulo,
                        espacio,
                        precio
                );

        /*
         * FECHA Y HORA
         */

        String fecha =
                item.getFecha() != null
                ? item.getFecha()
                        .format(formatoFecha)
                : "Sin fecha";

        String hora =
                item.getHora() != null
                ? item.getHora()
                        .format(formatoHora)
                : "Sin hora";

        Label fechaHora =
                new Label(
                        "● " + fecha
                        + "     ◷ "
                        + hora
                        + " hrs"
                );

        fechaHora
                .getStyleClass()
                .add("movie-detail");

        /*
         * SALA
         */

        Label sala =
                new Label(
                        "▣ "
                        + valorTexto(
                                item.getNombreSala()
                        )
                );

        sala
                .getStyleClass()
                .add("movie-detail");

        /*
         * ESTADO
         */

        Label estado =
                new Label(
                        "Estado: "
                        + valorTexto(
                                item.getEstado()
                        )
                );

        estado
                .getStyleClass()
                .add("availability-badge");

        /*
         * ASIENTOS
         */

        Label asientos =
                new Label(
                        "● "
                        + item.getAsientosDisponibles()
                        + " asientos disponibles"
                );

        asientos
                .getStyleClass()
                .add("availability-badge");

        /*
         * ID DE FUNCIÓN
         */

        Label idFuncion =
                new Label(
                        "ID: F-"
                        + item.getIdFuncion()
                );

        idFuncion
                .getStyleClass()
                .add("movie-id");

        HBox estadoFila =
                new HBox(5);

        estadoFila.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
        );

        Region espacioEstado =
                new Region();

        HBox.setHgrow(
                espacioEstado,
                javafx.scene.layout.Priority.ALWAYS
        );

        estadoFila.getChildren()
                .addAll(
                        asientos,
                        espacioEstado,
                        idFuncion
                );

        /*
         * BOTÓN
         */

        Button verFuncion =
                new Button(
                        "Ver función"
                );

        verFuncion
                .setMaxWidth(
                        Double.MAX_VALUE
                );

        verFuncion
                .getStyleClass()
                .add("button-ver-funcion");

        informacion
                .getChildren()
                .addAll(
                        tituloPrecio,
                        fechaHora,
                        sala,
                        estado,
                        estadoFila,
                        verFuncion
                );

        tarjeta
                .getChildren()
                .addAll(
                        posterContainer,
                        informacion
                );

        return tarjeta;
    }

    /**
     * Carga el poster local correspondiente a la película.
     *
     * Se buscan archivos con el formato:
     *
     * pelicula_ID.jpg
     * pelicula_ID.png
     * pelicula_ID.jpeg
     *
     * @param imageView componente donde se cargará la imagen
     * @param idPelicula identificador de la película
     */
    private void cargarPoster(
            ImageView imageView,
            int idPelicula) {

        String[] extensiones = {
            ".jpg",
            ".png",
            ".jpeg"
        };

        for (String extension : extensiones) {

            String ruta =
                    "/org/cine/resources/images/"
                    + "pelicula_"
                    + idPelicula
                    + extension;

            InputStream entrada =
                    getClass()
                            .getResourceAsStream(ruta);

            if (entrada != null) {

                Image imagen =
                        new Image(entrada);

                imageView.setImage(imagen);

                return;
            }
        }
    }

    /**
     * Muestra un mensaje cuando no existen funciones.
     */
    private void mostrarSinResultados() {

        VBox mensaje =
                new VBox(8);

        mensaje.setAlignment(
                javafx.geometry.Pos.CENTER
        );

        Label titulo =
                new Label(
                        "No se encontraron funciones"
                );

        titulo.setStyle(
                "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #10203a;"
        );

        Label descripcion =
                new Label(
                        "No existen funciones disponibles "
                        + "para mostrar."
                );

        descripcion.setStyle(
                "-fx-font-size: 10px;"
                + "-fx-text-fill: #708097;"
        );

        mensaje.getChildren()
                .addAll(
                        titulo,
                        descripcion
                );

        gridCartelera
                .getChildren()
                .add(mensaje);
    }

    /**
     * Convierte un precio a formato de moneda.
     *
     * @param precio precio de la función
     * @return precio formateado
     */
    private String formatearPrecio(
            BigDecimal precio) {

        if (precio == null) {
            return "Q0.00";
        }

        return String.format(
                "Q%.2f",
                precio.doubleValue()
        );
    }

    /**
     * Evita mostrar valores null.
     *
     * @param valor texto recibido
     * @return texto válido
     */
    private String valorTexto(String valor) {

        if (valor == null || valor.isBlank()) {
            return "No disponible";
        }

        return valor;
    }
}