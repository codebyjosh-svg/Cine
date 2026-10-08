package org.cine.dao;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.cine.model.Funcion;
import org.cine.model.Pelicula;
import org.cine.model.Sala;

/** Operaciones de US-2.2. Los errores SQL se comunican al controlador. */
public interface FuncionDAO {
    List<Funcion> listarTodos() throws SQLException;
    Optional<Funcion> buscarPorId(int idFuncion) throws SQLException;
    int insertar(Funcion funcion) throws SQLException;
    void actualizar(Funcion funcion) throws SQLException;
    void cancelar(int idFuncion) throws SQLException;
    void finalizar(int idFuncion) throws SQLException;
    List<Pelicula> listarPeliculasActivas() throws SQLException;
    List<Sala> listarSalasActivas() throws SQLException;
    LocalDateTime obtenerHoraServidor() throws SQLException;
}
