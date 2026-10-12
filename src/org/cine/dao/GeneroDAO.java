package org.cine.dao;

import java.util.List;
import org.cine.model.Genero;

public interface GeneroDAO {

    List<Genero> listarTodos();

    Genero buscarGenero(int idGenero);

    boolean insertar(Genero genero);

    boolean actualizar(Genero genero);

    boolean eliminar(int idGenero);

    boolean cambiarEstado(int idGenero, int estado);
}