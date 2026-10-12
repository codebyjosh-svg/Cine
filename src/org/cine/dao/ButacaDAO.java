package org.cine.dao;

import java.util.List;
import org.cine.model.Butaca;

public interface ButacaDAO {

    List<Butaca> listarTodos();

    List<Butaca> listarPorSala(int idSala);

    Butaca buscarButaca(int idButaca);

    boolean insertar(Butaca butaca);

    boolean actualizar(Butaca butaca);

    boolean eliminar(int idButaca);

    boolean cambiarEstado(int idButaca, int estado);

    String getUltimoError();
}
