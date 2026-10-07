package org.cine.system;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Principal extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        URL ruta = getClass().getResource(
                "/org/cine/view/Generos.fxml"
        );

        if (ruta == null) {
            throw new RuntimeException(
                    "No se encontró Generos.fxml"
            );
        }

        FXMLLoader loader = new FXMLLoader(ruta);

        Scene scene = new Scene(loader.load());

        stage.setTitle("Cinema");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}