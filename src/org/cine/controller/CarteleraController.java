package org.cine.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;

/**
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
    private TilePane gridCartelera;

    /**
     * Inicializa el controlador.
     */
    @FXML
    public void initialize() {
     
    }
}