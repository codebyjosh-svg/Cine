package org.cine.dao;

import java.util.List;
import org.cine.model.Sala;

public interface SalaDAO {

    List<Sala> listarTodos();

    Sala buscarSala(int idSala);

    boolean insertar(Sala sala);

    boolean actualizar(Sala sala);

    boolean eliminar(int idSala);

    boolean cambiarEstado(int idSala, int estado);

    String getUltimoError();
}
