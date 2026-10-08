package org.cine.dao;

import java.util.List;
import org.cine.model.Pelicula;

public interface PeliculaDAO {

    List<Pelicula> listarTodos();

    Pelicula buscarPelicula(int idPelicula);

    boolean insertar(Pelicula pelicula);

    boolean actualizar(Pelicula pelicula);

    boolean eliminar(int idPelicula);

    boolean cambiarEstado(int idPelicula, int estado);
}