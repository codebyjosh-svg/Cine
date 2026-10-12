package org.cine.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/** Ejecuta JDBC fuera del hilo de JavaFX y entrega el resultado en el hilo de la vista. */
public final class TareasFX {
    private TareasFX() { }

    public static <T> void ejecutar(Callable<T> trabajo, Consumer<T> exito,
            Consumer<Throwable> error, Consumer<Boolean> ocupado) {
        ocupado.accept(true);
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() throws Exception { return trabajo.call(); }
        };
        tarea.setOnSucceeded(evento -> {
            ocupado.accept(false);
            exito.accept(tarea.getValue());
        });
        tarea.setOnFailed(evento -> {
            ocupado.accept(false);
            error.accept(tarea.getException());
        });
        Thread hilo = new Thread(tarea, "cine-consulta");
        hilo.setDaemon(true);
        hilo.start();
    }
}
