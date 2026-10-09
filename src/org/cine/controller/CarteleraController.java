package org.cine.controller;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
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
 * Permite consultar las funciones disponibles,
 * aplicar filtros y seleccionar una función.
 *
 * La función seleccionada queda preparada para continuar
 * posteriormente hacia el flujo de venta.
 *
 * @author Joshua
 */
public class CarteleraController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private ComboBox<String> cbGenero;

    @FXML
    private ComboBox<String> cbSala;

    @FXML
    private Button btnVolver;

    @FXML
    private Button btnFiltrar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private TilePane gridCartelera;

    /**
     * DAO encargado de consultar la cartelera.
     */
    private final CarteleraDAO carteleraDAO =
            new CarteleraDAOImpl();

    /**
     * Formato utilizado para mostrar fechas.
     */
    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Formato utilizado para mostrar horas.
     */
    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Lista completa obtenida desde la base de datos.
     */
    private List<CarteleraItem> carteleraCompleta =
            new ArrayList<>();

    /**
     * Función actualmente seleccionada.
     *
     * Esta función puede ser obtenida posteriormente
     * por el flujo de venta.
     */
    private CarteleraItem funcionSeleccionada;

    /**
     * Tarjeta actualmente seleccionada.
     */
    private VBox tarjetaSeleccionada;

    /**
     * Botón de la tarjeta seleccionada.
     */
    private Button botonSeleccionado;

    /**
     * Inicializa la pantalla.
     */
    @FXML
    public void initialize() {

        cargarCartelera();
    }

    /**
     * Obtiene las funciones disponibles desde la base de datos.
     */
    private void cargarCartelera() {

        try {

            carteleraCompleta =
                    carteleraDAO.listarCartelera();

            if (carteleraCompleta == null) {

                carteleraCompleta =
                        new ArrayList<>();
            }

            cargarOpcionesFiltros();

            mostrarFunciones(
                    carteleraCompleta
            );

        } catch (SQLException ex) {

            gridCartelera
                    .getChildren()
                    .clear();

            Label error =
                    new Label(
                            "No se pudo cargar la cartelera."
                    );

            error.setStyle(
                    "-fx-text-fill: #b91c1c;"
                    + "-fx-font-size: 13px;"
                    + "-fx-font-weight: bold;"
            );

            gridCartelera
                    .getChildren()
                    .add(error);

            ex.printStackTrace();
        }
    }

    /**
     * Carga los géneros y salas disponibles
     * en los ComboBox.
     */
    private void cargarOpcionesFiltros() {

        Set<String> generos =
                new LinkedHashSet<>();

        Set<String> salas =
                new LinkedHashSet<>();

        for (CarteleraItem item :
                carteleraCompleta) {

            if (item.getGenero() != null
                    && !item.getGenero().isBlank()) {

                generos.add(
                        item.getGenero()
                );
            }

            if (item.getNombreSala() != null
                    && !item.getNombreSala().isBlank()) {

                salas.add(
                        item.getNombreSala()
                );
            }
        }

        cbGenero.getItems().clear();

        cbGenero
                .getItems()
                .add("Todos los géneros");

        cbGenero
                .getItems()
                .addAll(
                        generos.stream()
                                .sorted(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                                .collect(
                                        Collectors.toList()
                                )
                );

        cbGenero
                .getSelectionModel()
                .selectFirst();

        cbSala.getItems().clear();

        cbSala
                .getItems()
                .add("Todas las salas");

        cbSala
                .getItems()
                .addAll(
                        salas.stream()
                                .sorted(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                                .collect(
                                        Collectors.toList()
                                )
                );

        cbSala
                .getSelectionModel()
                .selectFirst();
    }

    /**
     * Aplica los filtros seleccionados.
     *
     * Los filtros pueden utilizarse individualmente
     * o combinados entre sí.
     */
    @FXML
    public void handleFiltrar() {

        String texto =
                txtBuscar.getText() == null
                        ? ""
                        : txtBuscar.getText()
                                .trim()
                                .toLowerCase();

        LocalDate fecha =
                dpFecha.getValue();

        String genero =
                cbGenero.getValue();

        String sala =
                cbSala.getValue();

        List<CarteleraItem> resultados =
                carteleraCompleta
                        .stream()
                        .filter(item -> {

                            boolean coincideTitulo =
                                    texto.isBlank()
                                    || (
                                            item.getTituloPelicula()
                                                    != null
                                            && item.getTituloPelicula()
                                                    .toLowerCase()
                                                    .contains(texto)
                                    );

                            boolean coincideFecha =
                                    fecha == null
                                    || fecha.equals(
                                            item.getFecha()
                                    );

                            boolean coincideGenero =
                                    genero == null
                                    || genero.equals(
                                            "Todos los géneros"
                                    )
                                    || (
                                            item.getGenero()
                                                    != null
                                            && item.getGenero()
                                                    .equalsIgnoreCase(
                                                            genero
                                                    )
                                    );

                            boolean coincideSala =
                                    sala == null
                                    || sala.equals(
                                            "Todas las salas"
                                    )
                                    || (
                                            item.getNombreSala()
                                                    != null
                                            && item.getNombreSala()
                                                    .equalsIgnoreCase(
                                                            sala
                                                    )
                                    );

                            return coincideTitulo
                                    && coincideFecha
                                    && coincideGenero
                                    && coincideSala;
                        })
                        .collect(
                                Collectors.toList()
                        );

        limpiarSeleccion();

        mostrarFunciones(resultados);
    }

    /**
     * Limpia todos los filtros y vuelve a mostrar
     * todas las funciones.
     */
    @FXML
    public void handleLimpiar() {

        txtBuscar.clear();

        dpFecha.setValue(null);

        if (!cbGenero.getItems().isEmpty()) {

            cbGenero
                    .getSelectionModel()
                    .selectFirst();
        }

        if (!cbSala.getItems().isEmpty()) {

            cbSala
                    .getSelectionModel()
                    .selectFirst();
        }

        limpiarSeleccion();

        mostrarFunciones(
                carteleraCompleta
        );
    }

    /**
     * Muestra una lista de funciones en el TilePane.
     *
     * @param lista funciones a mostrar
     */
    private void mostrarFunciones(
            List<CarteleraItem> lista) {

        gridCartelera
                .getChildren()
                .clear();

        if (lista == null
                || lista.isEmpty()) {

            mostrarSinResultados();

            return;
        }

        for (CarteleraItem item :
                lista) {

            gridCartelera
                    .getChildren()
                    .add(
                            crearTarjeta(item)
                    );
        }
    }

    /**
     * Crea una tarjeta visual para una función.
     *
     * @param item función de cartelera
     * @return tarjeta creada
     */
    private VBox crearTarjeta(
            CarteleraItem item) {

        VBox tarjeta =
                new VBox(0);

        tarjeta.setPrefWidth(275);
        tarjeta.setMaxWidth(275);

        tarjeta
                .getStyleClass()
                .add("movie-card");

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

        poster
                .getStyleClass()
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

        HBox badges =
                new HBox(4);

        badges.setPadding(
                new Insets(6)
        );

        StackPane.setAlignment(
                badges,
                Pos.TOP_RIGHT
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
                .add(
                        "classification-badge"
                );

        Label genero =
                new Label(
                        valorTexto(
                                item.getGenero()
                        )
                );

        genero
                .getStyleClass()
                .add("genre-badge");

        badges
                .getChildren()
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
                Pos.CENTER_LEFT
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

        tituloPrecio
                .getChildren()
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
                Pos.CENTER_LEFT
        );

        Region espacioEstado =
                new Region();

        HBox.setHgrow(
                espacioEstado,
                javafx.scene.layout.Priority.ALWAYS
        );

        estadoFila
                .getChildren()
                .addAll(
                        asientos,
                        espacioEstado,
                        idFuncion
                );

        /*
         * ==============================
         * BOTÓN SELECCIONAR
         * ==============================
         */

        Button verFuncion =
                new Button(
                        "Ver función"
                );

        verFuncion.setMaxWidth(
                Double.MAX_VALUE
        );

        verFuncion
                .getStyleClass()
                .add(
                        "button-ver-funcion"
                );

        verFuncion.setOnAction(
                evento ->
                        seleccionarFuncion(
                                item,
                                tarjeta,
                                verFuncion
                        )
        );

        /*
         * ==============================
         * AGREGAR ELEMENTOS
         * ==============================
         */

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
     * Selecciona una función.
     *
     * La función seleccionada queda almacenada
     * en funcionSeleccionada para poder ser utilizada
     * posteriormente por el flujo de venta.
     *
     * @param item función seleccionada
     * @param tarjeta tarjeta visual
     * @param boton botón de selección
     */
    private void seleccionarFuncion(
            CarteleraItem item,
            VBox tarjeta,
            Button boton) {

        /*
         * Quitar selección anterior.
         */

        if (tarjetaSeleccionada != null) {

            tarjetaSeleccionada
                    .getStyleClass()
                    .remove(
                            "movie-card-selected"
                    );
        }

        if (botonSeleccionado != null) {

            botonSeleccionado
                    .setText(
                            "Ver función"
                    );
        }

        /*
         * Guardar nueva selección.
         */

        funcionSeleccionada = item;

        tarjetaSeleccionada = tarjeta;

        botonSeleccionado = boton;

        /*
         * Marcar visualmente.
         */

        tarjetaSeleccionada
                .getStyleClass()
                .add(
                        "movie-card-selected"
                );

        botonSeleccionado
                .setText(
                        "Función seleccionada"
                );

        System.out.println(
                "Función seleccionada: "
                + item.getIdFuncion()
        );
    }

    /**
     * Elimina la selección actual.
     */
    private void limpiarSeleccion() {

        if (tarjetaSeleccionada != null) {

            tarjetaSeleccionada
                    .getStyleClass()
                    .remove(
                            "movie-card-selected"
                    );
        }

        if (botonSeleccionado != null) {

            botonSeleccionado
                    .setText(
                            "Ver función"
                    );
        }

        tarjetaSeleccionada = null;

        botonSeleccionado = null;

        funcionSeleccionada = null;
    }

    /**
     * Obtiene la función actualmente seleccionada.
     *
     * Este método permite que el flujo de venta
     * pueda recibir la función seleccionada.
     *
     * @return función seleccionada o null
     */
    public CarteleraItem getFuncionSeleccionada() {

        return funcionSeleccionada;
    }

    /**
     * Prepara la función seleccionada para continuar
     * hacia el flujo de venta.
     *
     * Este método evita continuar si el usuario
     * todavía no ha seleccionado una función.
     *
     * @return función seleccionada
     * @throws IllegalStateException si no existe selección
     */
    public CarteleraItem prepararFuncionParaVenta() {

        if (funcionSeleccionada == null) {

            throw new IllegalStateException(
                    "Debe seleccionar una función antes de continuar."
            );
        }

        System.out.println(
                "Función enviada al flujo de venta: "
                + funcionSeleccionada.getIdFuncion()
        );

        return funcionSeleccionada;
    }

    /**
     * Muestra un mensaje cuando se intenta utilizar
     * la función seleccionada sin haber realizado
     * una selección.
     */
    public void validarSeleccionParaVenta() {

        try {

            CarteleraItem funcion =
                    prepararFuncionParaVenta();

            Alert alerta =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alerta.setTitle(
                    "Función seleccionada"
            );

            alerta.setHeaderText(
                    "Función lista para venta"
            );

            alerta.setContentText(
                    "Película: "
                    + valorTexto(
                            funcion.getTituloPelicula()
                    )
                    + "\nSala: "
                    + valorTexto(
                            funcion.getNombreSala()
                    )
                    + "\nFecha: "
                    + (
                            funcion.getFecha() != null
                            ? funcion.getFecha()
                                    .format(formatoFecha)
                            : "Sin fecha"
                    )
                    + "\nHora: "
                    + (
                            funcion.getHora() != null
                            ? funcion.getHora()
                                    .format(formatoHora)
                            : "Sin hora"
                    )
                    + "\nID función: "
                    + funcion.getIdFuncion()
            );

            alerta.showAndWait();

        } catch (IllegalStateException ex) {

            Alert alerta =
                    new Alert(
                            Alert.AlertType.WARNING
                    );

            alerta.setTitle(
                    "Selección requerida"
            );

            alerta.setHeaderText(
                    "No hay una función seleccionada"
            );

            alerta.setContentText(
                    ex.getMessage()
            );

            alerta.showAndWait();
        }
    }

    /**
     * Carga el poster local correspondiente a la película.
     *
     * @param imageView componente donde se cargará la imagen
     * @param idPelicula identificador de película
     */
    private void cargarPoster(
            ImageView imageView,
            int idPelicula) {

        String[] extensiones = {
            ".jpg",
            ".png",
            ".jpeg"
        };

        for (String extension :
                extensiones) {

            String ruta =
                    "/org/cine/resources/images/"
                    + "pelicula_"
                    + idPelicula
                    + extension;

            try (InputStream entrada =
                    getClass()
                            .getResourceAsStream(
                                    ruta
                            )) {

                if (entrada != null) {

                    Image imagen =
                            new Image(
                                    entrada
                            );

                    imageView
                            .setImage(
                                    imagen
                            );

                    return;
                }

            } catch (Exception ex) {

                System.out.println(
                        "No se pudo cargar el poster: "
                        + ruta
                );
            }
        }
    }

    /**
     * Muestra el mensaje cuando no existen
     * resultados para los filtros.
     */
    private void mostrarSinResultados() {

        VBox mensaje =
                new VBox(8);

        mensaje.setAlignment(
                Pos.CENTER
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
                        "Intenta cambiar los filtros de búsqueda."
                );

        descripcion.setStyle(
                "-fx-font-size: 10px;"
                + "-fx-text-fill: #708097;"
        );

        mensaje
                .getChildren()
                .addAll(
                        titulo,
                        descripcion
                );

        gridCartelera
                .getChildren()
                .add(mensaje);
    }

    /**
     * Formatea el precio.
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
     * @param valor texto
     * @return texto válido
     */
    private String valorTexto(
            String valor) {

        if (valor == null
                || valor.isBlank()) {

            return "No disponible";
        }

        return valor;
    }
}