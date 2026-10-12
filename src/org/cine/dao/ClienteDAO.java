package org.cine.dao;

import java.util.List;
import org.cine.model.Cliente;

public interface ClienteDAO {

    List<Cliente> listarTodos();

    Cliente buscarCliente(int idCliente);

    boolean insertar(Cliente cliente);

    boolean actualizar(Cliente cliente);

    boolean eliminar(int idCliente);

    boolean cambiarEstado(int idCliente, int estado);

    String getUltimoError();
}